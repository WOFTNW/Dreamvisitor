package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

public class CommandScheduler {
  private static CommandScheduler instance;
  private final List<Schedule> schedules = new ArrayList<>();
  private final File configFile;
  private FileConfiguration config;
  private int taskId = -1;
  // how frequent to run the Scheduler checks
  // unit is in Game Tick
  // For 20 game ticks is one sec
  private long updateTick = 20L * 5L;

  private CommandScheduler() {
    // Initialize config file
    configFile = new File(Dreamvisitor.getPlugin().getDataFolder(), "schedules.yml");
    loadConfig();

    // Start scheduler
    startScheduler();
  }

  public static CommandScheduler getInstance() {
    if (instance == null) {
      instance = new CommandScheduler();
    }
    return instance;
  }

  /**
   * Load schedules from configuration
   */
  public void loadConfig() {
    schedules.clear();
    if (!configFile.exists()) {
      try {
        configFile.createNewFile();
      } catch (IOException e) {
        Dreamvisitor.getPlugin().getLogger().log(Level.SEVERE, "Could not create schedules.yml", e);
        return;
      }
    }

    config = YamlConfiguration.loadConfiguration(configFile);
    ConfigurationSection schedulesSection = config.getConfigurationSection("schedules");

    if (schedulesSection != null) {
      for (String name : schedulesSection.getKeys(false)) {
        ConfigurationSection scheduleSection = schedulesSection.getConfigurationSection(name);
        if (scheduleSection != null) {
          String type = scheduleSection.getString("type", "interval");

          switch (type) {
            case "interval" -> {
              int intervalMinutes = scheduleSection.getInt("interval-minutes");
              loadCommands(name, type, intervalMinutes, scheduleSection);
            }
            case "daily" -> {
              String timeString = scheduleSection.getString("time");
              if (timeString != null) {
                try {
                  LocalTime time = LocalTime.parse(timeString);
                  loadCommands(name, type, time, scheduleSection);
                } catch (DateTimeParseException e) {
                  Dreamvisitor.getPlugin().getLogger()
                      .warning("Invalid time format for schedule " + name + ": " + timeString);
                }
              }
            }
            case "cron" -> {
              String pattern = scheduleSection.getString("pattern");
              if (pattern != null) {
                try {
                  CronPattern cronPattern = new CronPattern(pattern);
                  loadCommands(name, type, cronPattern, scheduleSection);
                } catch (PatternSyntaxException e) {
                  Dreamvisitor.getPlugin().getLogger()
                      .warning("Invalid cron pattern for schedule " + name + ": " + pattern);
                }
              }
            }
            default -> Dreamvisitor.getPlugin().getLogger().warning("Unknown schedule type: " + type);
          }
        }
      }
    }
  }

  private void loadCommands(String name, String type, Object timeSpec, ConfigurationSection section) {
    List<String> commandList = section.getStringList("commands");
    Map<Integer, Integer> delays = new HashMap<>();

    ConfigurationSection delaysSection = section.getConfigurationSection("delays");
    if (delaysSection != null) {
      for (String key : delaysSection.getKeys(false)) {
        try {
          int index = Integer.parseInt(key);
          int delay = delaysSection.getInt(key);
          delays.put(index, delay);
        } catch (NumberFormatException e) {
          Dreamvisitor.getPlugin().getLogger().warning("Invalid delay index: " + key);
        }
      }
    }

    long lastRun = section.getLong("last-run", 0);

    Schedule schedule;
    if (timeSpec instanceof Integer intervalMinutes) {
      // Convert minutes to ticks (20 ticks/second * 60 seconds/minute)
      int intervalTicks = intervalMinutes * 20 * 60;
      schedule = new Schedule(name, intervalTicks, commandList, delays);
    } else if (timeSpec instanceof LocalTime time) {
      schedule = new Schedule(name, time, commandList, delays);
    } else if (timeSpec instanceof CronPattern pattern) {
      schedule = new Schedule(name, pattern, commandList, delays);
    } else {
      return;
    }

    if (lastRun > 0) {
      schedule.setLastRun(LocalDateTime.ofEpochSecond(lastRun, 0, java.time.ZoneOffset.UTC));
    }

    schedules.add(schedule);
  }

  /**
   * Saves the current schedules to the configuration file.
   * <p>
   * This method serializes all schedule objects to YAML format and writes them to
   * the schedules.yml file. Different schedule types (interval, daily, cron) are
   * handled with appropriate type-specific properties. The method also saves
   * metadata
   * such as commands, delays, and last execution time.
   * <p>
   * If the save operation fails, an error is logged, but execution continues.
   */
  public void saveConfig() {
    // Clear existing schedules section to prevent stale data
    config.set("schedules", null);

    // Iterate through all schedules and save each one
    for (Schedule schedule : schedules) {
      String path = "schedules." + schedule.getName();

      // Save type-specific properties based on schedule type
      switch (schedule.getType()) {
        case INTERVAL -> {
          config.set(path + ".type", "interval");
          // Convert ticks back to minutes for config storage for backward compatibility
          // This allows older versions to read the configuration correctly
          // TODO: Fix this as it cause losses in time units
          config.set(path + ".interval-minutes", schedule.getIntervalMinutes());
        }
        case DAILY -> {
          config.set(path + ".type", "daily");
          // Format time using standard HH:mm:ss format for consistency
          config.set(path + ".time", schedule.getDailyTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        case CRON -> {
          config.set(path + ".type", "cron");
          // Store the raw cron pattern string
          config.set(path + ".pattern", schedule.getCronPattern().getPattern());
        }
      }

      config.set(path + ".commands", schedule.getCommands());

      // Save delays
      Map<Integer, Integer> delays = schedule.getDelays();
      if (!delays.isEmpty()) {
        for (Map.Entry<Integer, Integer> entry : delays.entrySet()) {
          config.set(path + ".delays." + entry.getKey(), entry.getValue());
        }
      }

      if (schedule.getLastRun() != null) {
        config.set(path + ".last-run", schedule.getLastRun().toEpochSecond(java.time.ZoneOffset.UTC));
      }
    }

    try {
      config.save(configFile);
    } catch (IOException e) {
      Dreamvisitor.getPlugin().getLogger().log(Level.SEVERE, "Could not save schedules.yml", e);
    }
  }

  /**
   * Start the scheduler task
   */
  public void startScheduler() {
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
    }

    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Dreamvisitor.getPlugin(), this::checkSchedules, 20L,
        updateTick);
  }

  /**
   * Stop the scheduler task
   */
  public void stopScheduler() {
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
      taskId = -1;
    }
  }

  /**
   * Check if any schedules need to be run
   */
  private void checkSchedules() {
    LocalDateTime now = LocalDateTime.now();

    for (Schedule schedule : new ArrayList<>(schedules)) {
      if (schedule.shouldRun(now)) {
        executeSchedule(schedule);
      }
    }
  }

  /**
   * Execute a scheduled command
   *
   * @param schedule The schedule to execute
   */
  private void executeSchedule(Schedule schedule) {
    Messager.debug("Executing scheduled commands for: " + schedule.getName());

    final AtomicBoolean success = new AtomicBoolean(true);
    List<String> commands = schedule.getCommands();
    Map<Integer, Integer> delays = schedule.getDelays();

    schedule.setLastRun(LocalDateTime.now());
    saveConfig();

    for (int i = 0; i < commands.size(); i++) {
      final int index = i;
      final String command = commands.get(i);

      // Get delay for this command in ticks (no conversion needed)
      int delayTicks = 0;
      if (delays.containsKey(index)) {
        delayTicks = delays.get(index);
      }

      Bukkit.getScheduler().runTaskLater(Dreamvisitor.getPlugin(), () -> {
        Messager.debug("Executing command " + (index + 1) + "/" + commands.size() + ": " + command);
        try {
          boolean result = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
          if (!result) {
            Dreamvisitor.getPlugin().getLogger().warning("Failed to execute scheduled command: " + command);
            success.set(false);
          }
        } catch (Exception e) {
          Dreamvisitor.getPlugin().getLogger().log(Level.SEVERE, "Error executing scheduled command: " + command, e);
          success.set(false);
        }
      }, delayTicks);
    }
  }

  /**
   * Add a new interval-based schedule
   *
   * @param name            The name of the schedule
   * @param intervalMinutes The interval in minutes
   * @param commands        The commands to run
   * @return The created schedule
   */
  public Schedule addSchedule(String name, int intervalMinutes, List<String> commands) {
    return addSchedule(name, intervalMinutes, commands, new HashMap<>());
  }

  /**
   * Add a new interval-based schedule
   *
   * @param name            The name of the schedule
   * @param intervalMinutes The interval in minutes
   * @param command         Single command to run
   * @return The created schedule
   */
  public Schedule addSchedule(String name, int intervalMinutes, String command) {
    return addSchedule(name, intervalMinutes, Collections.singletonList(command));
  }

  /**
   * Add a new interval-based schedule with delays
   *
   * @param name            The name of the schedule
   * @param intervalMinutes The interval in minutes
   * @param commands        The commands to run
   * @param delays          Map of command index to delay in ticks
   * @return The created schedule
   */
  public Schedule addSchedule(String name, int intervalMinutes, List<String> commands, Map<Integer, Integer> delays) {
    // Remove existing schedule with the same name
    removeSchedule(name);

    // Convert minutes to ticks (20 ticks/second * 60 seconds/minute)
    int intervalTicks = intervalMinutes * 20 * 60;
    Schedule schedule = new Schedule(name, intervalTicks, commands, delays);
    schedules.add(schedule);
    saveConfig();
    return schedule;
  }

  /**
   * Add a new daily schedule
   *
   * @param name     The name of the schedule
   * @param time     The time of day to run
   * @param commands The commands to run
   * @return The created schedule
   */
  public Schedule addDailySchedule(String name, LocalTime time, List<String> commands) {
    return addDailySchedule(name, time, commands, new HashMap<>());
  }

  /**
   * Add a new daily schedule
   *
   * @param name    The name of the schedule
   * @param time    The time of day to run
   * @param command Single command to run
   * @return The created schedule
   */
  public Schedule addDailySchedule(String name, LocalTime time, String command) {
    return addDailySchedule(name, time, Collections.singletonList(command));
  }

  /**
   * Add a new daily schedule with delays
   *
   * @param name     The name of the schedule
   * @param time     The time of day to run
   * @param commands The commands to run
   * @param delays   Map of command index to delay in ticks
   * @return The created schedule
   */
  public Schedule addDailySchedule(String name, LocalTime time, List<String> commands, Map<Integer, Integer> delays) {
    // Remove existing schedule with the same name
    removeSchedule(name);

    Schedule schedule = new Schedule(name, time, commands, delays);
    schedules.add(schedule);
    saveConfig();
    return schedule;
  }

  /**
   * Add a new cron-style schedule
   *
   * @param name     The name of the schedule
   * @param pattern  The cron pattern
   * @param commands The commands to run
   * @return The created schedule
   */
  public Schedule addCronSchedule(String name, String pattern, List<String> commands) {
    return addCronSchedule(name, pattern, commands, new HashMap<>());
  }

  /**
   * Add a new cron-style schedule
   *
   * @param name    The name of the schedule
   * @param pattern The cron pattern
   * @param command Single command to run
   * @return The created schedule
   */
  public Schedule addCronSchedule(String name, String pattern, String command) {
    return addCronSchedule(name, pattern, Collections.singletonList(command));
  }

  /**
   * Add a new cron-style schedule with delays
   *
   * @param name     The name of the schedule
   * @param pattern  The cron pattern
   * @param commands The commands to run
   * @param delays   Map of command index to delay in ticks
   * @return The created schedule
   */
  public Schedule addCronSchedule(String name, String pattern, List<String> commands, Map<Integer, Integer> delays) {
    // Remove existing schedule with the same name
    removeSchedule(name);

    try {
      CronPattern cronPattern = new CronPattern(pattern);
      Schedule schedule = new Schedule(name, cronPattern, commands, delays);
      schedules.add(schedule);
      saveConfig();
      return schedule;
    } catch (PatternSyntaxException e) {
      Dreamvisitor.getPlugin().getLogger().log(Level.SEVERE, "Invalid cron pattern: " + pattern, e);
      return null;
    }
  }

  /**
   * Remove a schedule
   *
   * @param name The name of the schedule
   * @return True if removed, false if not found
   */
  public boolean removeSchedule(String name) {
    for (Schedule schedule : new ArrayList<>(schedules)) {
      if (schedule.getName().equals(name)) {
        schedules.remove(schedule);
        saveConfig();
        return true;
      }
    }
    return false;
  }

  /**
   * Get all schedules
   *
   * @return An unmodifiable list of schedules
   */
  public List<Schedule> getSchedules() {
    return Collections.unmodifiableList(schedules);
  }

  /**
   * Run a schedule immediately
   *
   * @param name The name of the schedule to run
   * @return True if run, false if not found
   */
  public boolean runScheduleNow(String name) {
    for (Schedule schedule : schedules) {
      if (schedule.getName().equals(name)) {
        executeSchedule(schedule);
        return true;
      }
    }
    return false;
  }

  /**
   * Get a schedule by name
   *
   * @param name The name of the schedule
   * @return The schedule or null if not found
   */
  @Nullable
  public Schedule getSchedule(String name) {
    for (Schedule schedule : schedules) {
      if (schedule.getName().equals(name)) {
        return schedule;
      }
    }
    return null;
  }

  /**
   * Add a delay to a command in a schedule
   *
   * @param name         The schedule name
   * @param commandIndex The command index (0-based)
   * @param delayTicks   The delay in ticks (20 ticks = 1 second)
   * @return True if successful, false if schedule not found or index invalid
   */
  public boolean addDelay(String name, int commandIndex, int delayTicks) {
    Schedule schedule = getSchedule(name);
    if (schedule == null || commandIndex < 0 || commandIndex >= schedule.getCommands().size()) {
      return false;
    }

    schedule.addDelay(commandIndex, delayTicks);
    saveConfig();
    return true;
  }

  /**
   * Remove a delay from a command in a schedule
   *
   * @param name         The schedule name
   * @param commandIndex The command index (0-based)
   * @return True if successful, false if schedule not found or no delay was set
   */
  public boolean removeDelay(String name, int commandIndex) {
    Schedule schedule = getSchedule(name);
    if (schedule == null) {
      return false;
    }

    boolean result = schedule.removeDelay(commandIndex);
    if (result) {
      saveConfig();
    }
    return result;
  }

  /**
   * Add a command to an existing schedule
   *
   * @param name    The schedule name
   * @param command The command to add
   * @return True if successful, false if schedule not found
   */
  public boolean addCommand(String name, String command) {
    Schedule schedule = getSchedule(name);
    if (schedule == null) {
      return false;
    }

    schedule.addCommand(command);
    saveConfig();
    return true;
  }

  /**
   * Remove a command from a schedule
   *
   * @param name         The schedule name
   * @param commandIndex The command index (0-based)
   * @return True if successful, false if schedule not found or index invalid
   */
  public boolean removeCommand(String name, int commandIndex) {
    Schedule schedule = getSchedule(name);
    if (schedule == null) {
      return false;
    }

    boolean result = schedule.removeCommand(commandIndex);
    if (result) {
      saveConfig();
    }
    return result;
  }

  /**
   * Simple implementation of cron pattern matching
   */
  public static class CronPattern {
    // Format: minute hour day-of-month month day-of-week
    // Supported: numbers, *, ranges (1-5), lists (1,2,3), step values (*/5, 1-5/2)
    private final String pattern;
    private final String minutePattern;
    private final String hourPattern;
    private final String dayOfMonthPattern;
    private final String monthPattern;
    private final String dayOfWeekPattern;

    public CronPattern(String pattern) throws PatternSyntaxException {
      this.pattern = pattern;
      String[] parts = pattern.split("\\s+");
      if (parts.length != 5) {
        throw new PatternSyntaxException("Cron pattern must have 5 parts", pattern, -1);
      }
      minutePattern = parts[0];
      hourPattern = parts[1];
      dayOfMonthPattern = parts[2];
      monthPattern = parts[3];
      dayOfWeekPattern = parts[4];

      // Validate all patterns
      validateField(minutePattern, 0, 59);
      validateField(hourPattern, 0, 23);
      validateField(dayOfMonthPattern, 1, 31);
      validateField(monthPattern, 1, 12);
      validateField(dayOfWeekPattern, 0, 6);
    }

    private void validateField(String field, int min, int max) throws PatternSyntaxException {
      if (field.equals("*")) {
        return;
      }

      if (field.contains("/")) {
        String[] parts = field.split("/");
        if (parts.length != 2) {
          throw new PatternSyntaxException("Invalid step value format", field, -1);
        }
        validateField(parts[0], min, max);
        try {
          int step = Integer.parseInt(parts[1]);
          if (step <= 0) {
            throw new PatternSyntaxException("Step value must be positive", field, -1);
          }
        } catch (NumberFormatException e) {
          throw new PatternSyntaxException("Invalid step value", field, -1);
        }
        return;
      }

      if (field.contains(",")) {
        for (String part : field.split(",")) {
          validateField(part, min, max);
        }
        return;
      }

      if (field.contains("-")) {
        String[] parts = field.split("-");
        if (parts.length != 2) {
          throw new PatternSyntaxException("Invalid range format", field, -1);
        }
        try {
          int start = Integer.parseInt(parts[0]);
          int end = Integer.parseInt(parts[1]);
          if (start < min || end > max || start > end) {
            throw new PatternSyntaxException("Invalid range values", field, -1);
          }
        } catch (NumberFormatException e) {
          throw new PatternSyntaxException("Invalid range values", field, -1);
        }
        return;
      }

      try {
        int value = Integer.parseInt(field);
        if (value < min || value > max) {
          throw new PatternSyntaxException("Value out of range", field, -1);
        }
      } catch (NumberFormatException e) {
        throw new PatternSyntaxException("Invalid value", field, -1);
      }
    }

    public String getPattern() {
      return pattern;
    }

    public boolean matches(LocalDateTime dateTime) {
      return matchesField(minutePattern, dateTime.getMinute(), 0, 59)
          && matchesField(hourPattern, dateTime.getHour(), 0, 23)
          && matchesField(dayOfMonthPattern, dateTime.getDayOfMonth(), 1, 31)
          && matchesField(monthPattern, dateTime.getMonthValue(), 1, 12)
          && matchesField(dayOfWeekPattern, dateTime.getDayOfWeek().getValue() % 7, 0, 6);
    }

    private boolean matchesField(String pattern, int value, int min, int max) {
      if (pattern.equals("*")) {
        return true;
      }

      if (pattern.contains("/")) {
        String[] parts = pattern.split("/");
        String range = parts[0];
        int step = Integer.parseInt(parts[1]);

        if (range.equals("*")) {
          return (value - min) % step == 0;
        } else {
          return matchesField(range, value, min, max) && (value - min) % step == 0;
        }
      }

      if (pattern.contains(",")) {
        for (String part : pattern.split(",")) {
          if (matchesField(part, value, min, max)) {
            return true;
          }
        }
        return false;
      }

      if (pattern.contains("-")) {
        String[] parts = pattern.split("-");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);
        return value >= start && value <= end;
      }

      return Integer.parseInt(pattern) == value;
    }
  }

  /**
   * Class representing a scheduled command
   */
  public static class Schedule {
    private final String name;
    private final ScheduleType type;
    private final int intervalTicks; // Now storing interval in ticks instead of minutes
    private final LocalTime dailyTime;
    private final CronPattern cronPattern;
    private final List<String> commands;
    private final Map<Integer, Integer> delays; // Command index -> delay in ticks
    private LocalDateTime lastRun;

    public enum ScheduleType {
      INTERVAL, DAILY, CRON
    }

    // Interval constructor - now accepts ticks instead of minutes
    public Schedule(String name, int intervalTicks, List<String> commands, Map<Integer, Integer> delays) {
      this.name = name;
      this.type = ScheduleType.INTERVAL;
      this.intervalTicks = intervalTicks;
      this.dailyTime = null;
      this.cronPattern = null;
      this.commands = new ArrayList<>(commands);
      this.delays = new HashMap<>(delays);
      this.lastRun = null;
    }

    // Daily constructor
    public Schedule(String name, LocalTime time, List<String> commands, Map<Integer, Integer> delays) {
      this.name = name;
      this.type = ScheduleType.DAILY;
      this.intervalTicks = 0;
      this.dailyTime = time;
      this.cronPattern = null;
      this.commands = new ArrayList<>(commands);
      this.delays = new HashMap<>(delays);
      this.lastRun = null;
    }

    // Cron constructor
    public Schedule(String name, CronPattern pattern, List<String> commands, Map<Integer, Integer> delays) {
      this.name = name;
      this.type = ScheduleType.CRON;
      this.intervalTicks = 0;
      this.dailyTime = null;
      this.cronPattern = pattern;
      this.commands = new ArrayList<>(commands);
      this.delays = new HashMap<>(delays);
      this.lastRun = null;
    }

    public String getName() {
      return name;
    }

    public ScheduleType getType() {
      return type;
    }

    /**
     * Get the interval in minutes (converted from ticks for backward compatibility)
     *
     * @return The interval in minutes
     */
    public int getIntervalMinutes() {
      return intervalTicks / (20 * 60); // Convert ticks to minutes
    }

    /**
     * Get the interval in ticks
     *
     * @return The interval in ticks
     */
    public int getIntervalTicks() {
      return intervalTicks;
    }

    public LocalTime getDailyTime() {
      return dailyTime;
    }

    public CronPattern getCronPattern() {
      return cronPattern;
    }

    public List<String> getCommands() {
      return Collections.unmodifiableList(commands);
    }

    public Map<Integer, Integer> getDelays() {
      return Collections.unmodifiableMap(delays);
    }

    public LocalDateTime getLastRun() {
      return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
      this.lastRun = lastRun;
    }

    public void addCommand(String command) {
      this.commands.add(command);
    }

    public boolean removeCommand(int index) {
      if (index < 0 || index >= commands.size()) {
        return false;
      }
      commands.remove(index);

      // Remove any delays for this command
      delays.remove(index);

      // Adjust delays for higher indexed commands
      Map<Integer, Integer> newDelays = new HashMap<>();
      for (Map.Entry<Integer, Integer> entry : delays.entrySet()) {
        int delayIndex = entry.getKey();
        if (delayIndex > index) {
          newDelays.put(delayIndex - 1, entry.getValue());
        } else {
          newDelays.put(delayIndex, entry.getValue());
        }
      }
      delays.clear();
      delays.putAll(newDelays);
      return true;
    }

    public void addDelay(int commandIndex, int delayTicks) {
      delays.put(commandIndex, delayTicks);
    }

    public boolean removeDelay(int commandIndex) {
      return delays.remove(commandIndex) != null;
    }

    /**
     * Check if this schedule should run
     *
     * @param now The current time
     * @return True if it should run
     */
    public boolean shouldRun(@NotNull LocalDateTime now) {
      switch (type) {
        case INTERVAL:
          if (lastRun == null) {
            return true; // Never run before
          }
          Duration duration = Duration.between(lastRun, now);
          // Convert duration to ticks and compare with intervalTicks
          long durationTicks = duration.toSeconds() * 20;
          return durationTicks >= intervalTicks;

        case DAILY:
          if (lastRun != null && lastRun.toLocalDate().equals(now.toLocalDate())) {
            return false; // Already run today
          }
          LocalTime currentTime = now.toLocalTime();
          return currentTime.isAfter(dailyTime) || currentTime.equals(dailyTime);

        case CRON:
          if (lastRun != null) {
            LocalDateTime nextMinute = lastRun.plusMinutes(1).withSecond(0).withNano(0);
            if (now.isBefore(nextMinute)) {
              return false; // Don't run more than once per minute
            }
          }
          return cronPattern.matches(now);

        default:
          return false;
      }
    }

    /**
     * Get a user-friendly string representation of time until next run
     *
     * @return Time until next run as a string
     */
    public String getTimeUntilNextRun() {
      LocalDateTime now = LocalDateTime.now();

      switch (type) {
        case INTERVAL:
          if (lastRun == null) {
            return "Ready to run";
          }

          // Calculate next run time using ticks
          long durationSeconds = intervalTicks / 20;
          LocalDateTime nextRun = lastRun.plusSeconds(durationSeconds);
          Duration duration = Duration.between(now, nextRun);

          if (duration.isNegative()) {
            return "Ready to run";
          }

          long hours = duration.toHours();
          long minutes = duration.toMinutesPart();
          long seconds = duration.toSecondsPart();

          if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
          } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
          } else {
            return seconds + "s";
          }

        case DAILY:
          LocalDateTime nextRunTime = now.toLocalDate().atTime(dailyTime);
          if (now.isAfter(nextRunTime)) {
            nextRunTime = nextRunTime.plusDays(1);
          }

          Duration timeUntil = Duration.between(now, nextRunTime);
          long days = timeUntil.toDays();
          long dHours = timeUntil.toHoursPart();
          long dMinutes = timeUntil.toMinutesPart();

          if (days > 0) {
            return days + "d " + dHours + "h " + dMinutes + "m";
          } else if (dHours > 0) {
            return dHours + "h " + dMinutes + "m";
          } else {
            return dMinutes + "m";
          }

        case CRON:
          // For cron, we check the next few hours in 1-minute increments to find the next
          // match
          LocalDateTime checkTime = now;
          for (int i = 0; i < 24 * 60; i++) { // Check up to 24 hours ahead
            checkTime = checkTime.plusMinutes(1);
            if (cronPattern.matches(checkTime)) {
              Duration cronTimeUntil = Duration.between(now, checkTime);
              long cronHours = cronTimeUntil.toHours();
              long cronMinutes = cronTimeUntil.toMinutesPart();

              if (cronHours > 0) {
                return cronHours + "h " + cronMinutes + "m";
              } else {
                return cronMinutes + "m";
              }
            }
          }
          return "More than 24h";

        default:
          return "Unknown";
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("Schedule{name='").append(name).append("', type=").append(type);

      switch (type) {
        case INTERVAL -> {
          int minutes = intervalTicks / (20 * 60);
          int remainingSeconds = (intervalTicks % (20 * 60)) / 20;
          if (remainingSeconds > 0) {
            sb.append(", interval=").append(minutes).append("m ").append(remainingSeconds).append("s");
          } else {
            sb.append(", interval=").append(minutes).append("m");
          }
        }
        case DAILY -> sb.append(", time=").append(dailyTime);
        case CRON -> sb.append(", pattern='").append(cronPattern.getPattern()).append("'");
      }

      sb.append(", commands=").append(commands.size());
      if (!delays.isEmpty()) {
        sb.append(", delays=").append(delays);
      }

      return sb.append("}").toString();
    }
  }
}
