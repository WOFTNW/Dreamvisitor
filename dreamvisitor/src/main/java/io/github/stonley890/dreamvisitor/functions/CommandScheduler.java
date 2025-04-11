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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class CommandScheduler {
  private static CommandScheduler instance;
  private final List<Schedule> schedules = new ArrayList<>();
  private final File configFile;
  private FileConfiguration config;
  private int taskId = -1;

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
        // noinspection ResultOfMethodCallIgnored
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
          int intervalMinutes = scheduleSection.getInt("interval-minutes");
          String command = scheduleSection.getString("command");
          long lastRun = scheduleSection.getLong("last-run", 0);

          Schedule schedule = new Schedule(name, intervalMinutes, command);
          if (lastRun > 0) {
            schedule.setLastRun(LocalDateTime.ofEpochSecond(lastRun, 0, java.time.ZoneOffset.UTC));
          }

          schedules.add(schedule);
        }
      }
    }
  }

  /**
   * Save schedules to configuration
   */
  public void saveConfig() {
    config.set("schedules", null);

    for (Schedule schedule : schedules) {
      String path = "schedules." + schedule.getName();
      config.set(path + ".interval-minutes", schedule.getIntervalMinutes());
      config.set(path + ".command", schedule.getCommand());
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
        20L * 60L); // Check every minute
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
    Dreamvisitor.debug("Executing scheduled command: " + schedule.getName() + " - " + schedule.getCommand());

    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), schedule.getCommand());
    schedule.setLastRun(LocalDateTime.now());
    saveConfig();
  }

  /**
   * Add a new schedule
   *
   * @param name            The name of the schedule
   * @param intervalMinutes The interval in minutes
   * @param command         The command to run
   * @return The created schedule
   */
  public Schedule addSchedule(String name, int intervalMinutes, String command) {
    // Remove existing schedule with the same name
    removeSchedule(name);

    Schedule schedule = new Schedule(name, intervalMinutes, command);
    schedules.add(schedule);
    saveConfig();
    return schedule;
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
   * Class representing a scheduled command
   */
  public static class Schedule {
    private final String name;
    private final int intervalMinutes;
    private final String command;
    private LocalDateTime lastRun;

    public Schedule(String name, int intervalMinutes, String command) {
      this.name = name;
      this.intervalMinutes = intervalMinutes;
      this.command = command;
      this.lastRun = null;
    }

    public String getName() {
      return name;
    }

    public int getIntervalMinutes() {
      return intervalMinutes;
    }

    public String getCommand() {
      return command;
    }

    public LocalDateTime getLastRun() {
      return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
      this.lastRun = lastRun;
    }

    /**
     * Check if this schedule should run
     *
     * @param now The current time
     * @return True if it should run
     */
    public boolean shouldRun(@NotNull LocalDateTime now) {
      if (lastRun == null) {
        return true; // Never run before
      }

      Duration duration = Duration.between(lastRun, now);
      return duration.toMinutes() >= intervalMinutes;
    }

    /**
     * Get a user-friendly string representation of time until next run
     *
     * @return Time until next run as a string
     */
    public String getTimeUntilNextRun() {
      if (lastRun == null) {
        return "Ready to run";
      }

      LocalDateTime nextRun = lastRun.plusMinutes(intervalMinutes);
      Duration duration = Duration.between(LocalDateTime.now(), nextRun);

      if (duration.isNegative()) {
        return "Ready to run";
      }

      long hours = duration.toHours();
      long minutes = duration.toMinutesPart();

      if (hours > 0) {
        return hours + "h " + minutes + "m";
      } else {
        return minutes + "m";
      }
    }
  }
}

