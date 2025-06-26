package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.CommandScheduler;
import io.github.stonley890.dreamvisitor.functions.CommandScheduler.Schedule;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdSchedule implements DVCommand {

  @NotNull
  @Override
  public CommandAPICommand getCommand() {
    return new CommandAPICommand("schedule")
        .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
        .withHelp("Schedule commands to run at specific intervals or times.",
            "Schedule commands to run at specific intervals or times.")
        .withSubcommands(
            new CommandAPICommand("interval")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Add a new interval-based schedule.", "Add a new schedule that runs every X minutes.")
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("interval-minutes", 1))
                .withArguments(new GreedyStringArgument("command"))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  int intervalMinutes = (int) args.get("interval-minutes");
                  String commandInput = (String) args.get("command");

                  // Check if the command input contains multiple commands (separated by
                  // semicolons)
                  List<String> commands = new ArrayList<>();
                  if (commandInput.contains(";")) {
                    commands.addAll(Arrays.asList(commandInput.split(";")));
                    // Trim commands
                    for (int i = 0; i < commands.size(); i++) {
                      commands.set(i, commands.get(i).trim());
                    }
                  } else {
                    commands.add(commandInput);
                  }

                  CommandScheduler.getInstance().addSchedule(name, intervalMinutes, commands);
                    Messager.send(sender, "Added schedule '" + name + "' to run " +
                            Messager.nameOrCountString(commands.stream().map(cmd -> "\"".concat(cmd).concat("\"")).toList(), "command", "commands") +
                        " every " + intervalMinutes + " minutes.");
                }),
            new CommandAPICommand("daily")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Add a new daily schedule.", "Add a new schedule that runs once per day at a specific time.")
                .withArguments(new StringArgument("name"))
                .withArguments(new TextArgument("time").includeSuggestions(
                    ArgumentSuggestions.strings("00:00:00", "06:00:00", "12:00:00", "18:00:00")))
                .withArguments(new GreedyStringArgument("command"))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  String timeString = (String) args.get("time");
                  String commandInput = (String) args.get("command");

                  try {
                    LocalTime time = LocalTime.parse(timeString);

                    // Check if the command input contains multiple commands (separated by
                    // semicolons)
                    List<String> commands = new ArrayList<>();
                    if (commandInput.contains(";")) {
                      commands.addAll(Arrays.asList(commandInput.split(";")));
                      // Trim commands
                      for (int i = 0; i < commands.size(); i++) {
                        commands.set(i, commands.get(i).trim());
                      }
                    } else {
                      commands.add(commandInput);
                    }

                    CommandScheduler.getInstance().addDailySchedule(name, time, commands);
                      Messager.send(sender, "Added schedule '" + name + "' to run " +
                              Messager.nameOrCountString(commands.stream().map(cmd -> "\"".concat(cmd).concat("\"")).toList(), "command", "commands") +
                              " commands daily at " + timeString + ".");

                  } catch (DateTimeParseException e) {
                    Messager.sendDanger(sender,
                        "Invalid time format. Please use HH:MM:SS format (e.g., 06:00:00)");
                  }
                }),
            new CommandAPICommand("cron")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Add a new cron-style schedule.",
                    "Add a new schedule using cron pattern (minute hour day-of-month month day-of-week).")
                .withArguments(new StringArgument("name"))
                .withArguments(new TextArgument("pattern").includeSuggestions(
                    ArgumentSuggestions.strings("0 * * * *", "0 0 * * *", "0 0 * * 0", "*/5 * * * *")))
                .withArguments(new GreedyStringArgument("command"))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  String pattern = (String) args.get("pattern");
                  String commandInput = (String) args.get("command");

                  // Check if the command input contains multiple commands (separated by
                  // semicolons)
                  List<String> commands = new ArrayList<>();
                  if (commandInput.contains(";")) {
                    commands.addAll(Arrays.asList(commandInput.split(";")));
                    // Trim commands
                    for (int i = 0; i < commands.size(); i++) {
                      commands.set(i, commands.get(i).trim());
                    }
                  } else {
                    commands.add(commandInput);
                  }

                  Schedule schedule = CommandScheduler.getInstance().addCronSchedule(name, pattern, commands);
                  if (schedule == null) {
                    Messager.sendDanger(sender,"Invalid cron pattern. Format: minute hour day-of-month month day-of-week");
                  } else {
                      Messager.send(sender, "Added schedule '" + name + "' to run " +
                              Messager.nameOrCountString(commands.stream().map(cmd -> "\"".concat(cmd).concat("\"")).toList(), "command", "commands") +
                              " commands using cron pattern '" + pattern + "'.");
                  }
                }),
            new CommandAPICommand("remove")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Remove a scheduled command.", "Remove a scheduled command.")
                .withArguments(new StringArgument("name")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(info -> CommandScheduler.getInstance().getSchedules().stream()
                            .map(Schedule::getName)
                            .toArray(String[]::new))))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");

                  if (CommandScheduler.getInstance().removeSchedule(name)) {
                    Messager.send(sender, "Removed schedule '" + name + "'.");
                  } else {
                    Messager.sendDanger(sender, "No schedule with name '" + name + "' found.");
                  }
                }),
            new CommandAPICommand("list")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("List all scheduled commands.", "List all scheduled commands.")
                .executesNative((sender, args) -> {
                  List<Schedule> schedules = CommandScheduler.getInstance().getSchedules();
                  if (schedules.isEmpty()) {
                    Messager.send(sender, "No schedules configured.");
                    return;
                  }

                  Messager.send(sender, "Scheduled commands:");
                  for (Schedule schedule : schedules) {
                    List<String> commands = schedule.getCommands();
                    String typeInfo = getTypeInfo(schedule);

                    Messager.send(sender,ChatColor.YELLOW + schedule.getName() + ChatColor.WHITE +
                        ": "
                        + (commands.size() > 1 ? commands.size() + " commands "
                            : "'" + ChatColor.GRAY + commands.get(0) + ChatColor.WHITE + "' ")
                        +
                        typeInfo + ". " +
                        ChatColor.AQUA + schedule.getTimeUntilNextRun() + ChatColor.WHITE + " until next run.");

                    if (commands.size() > 1) {
                      sender.sendMessage(ChatColor.GRAY + "  Commands: ");
                      for (int i = 0; i < commands.size(); i++) {
                        sender.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + commands.get(i));
                      }
                    }
                  }
                }),
            new CommandAPICommand("run")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Run a scheduled command immediately.", "Run a scheduled command immediately.")
                .withArguments(new StringArgument("name")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(info -> CommandScheduler.getInstance().getSchedules().stream()
                            .map(Schedule::getName)
                            .toArray(String[]::new))))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");

                  if (CommandScheduler.getInstance().runScheduleNow(name)) {
                    Messager.send(sender, "Running schedule '" + name + "' now.");
                  } else {
                    Messager.sendDanger(sender, "No schedule with name '" + name + "' found.");
                  }
                }),
            new CommandAPICommand("add-command")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Add a command to an existing schedule.", "Add a command to an existing schedule.")
                .withArguments(new StringArgument("name")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(info -> CommandScheduler.getInstance().getSchedules().stream()
                            .map(Schedule::getName)
                            .toArray(String[]::new))))
                .withArguments(new GreedyStringArgument("command"))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  String command = (String) args.get("command");

                  if (CommandScheduler.getInstance().addCommand(name, command)) {
                    Messager.send(sender, "Added command '" + command + "' to schedule '" + name + "'.");
                  } else {
                    Messager.sendDanger(sender, "No schedule with name '" + name + "' found.");
                  }
                }),
            new CommandAPICommand("remove-command")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Remove a command from a schedule.", "Remove a command from a schedule.")
                .withArguments(new StringArgument("name")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(info -> CommandScheduler.getInstance().getSchedules().stream()
                            .map(Schedule::getName)
                            .toArray(String[]::new))))
                .withArguments(new IntegerArgument("index", 1))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  int index = (int) args.get("index") - 1; // Convert to 0-based index

                  if (CommandScheduler.getInstance().removeCommand(name, index)) {
                    Messager.send(sender, "Removed command at position " + (index + 1)
                        + " from schedule '" + name + "'.");
                  } else {
                    Messager.sendDanger(sender, "No schedule with name '" + name
                            + "' found or invalid command index.");
                  }
                }),
            new CommandAPICommand("add-delay")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Add a delay before executing a command in a schedule.",
                    "Add a delay before executing a command in a schedule (in game ticks, 20 ticks = 1 second).")
                .withArguments(new StringArgument("name")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(info -> CommandScheduler.getInstance().getSchedules().stream()
                            .map(Schedule::getName)
                            .toArray(String[]::new))))
                .withArguments(new IntegerArgument("index", 1))
                .withArguments(new IntegerArgument("delay-ticks", 1))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  int index = (int) args.get("index") - 1; // Convert to 0-based index
                  int delayTicks = (int) args.get("delay-ticks");

                  // Pass ticks directly to CommandScheduler without conversion
                  if (CommandScheduler.getInstance().addDelay(name, index, delayTicks)) {
                    Messager.send(sender, "Added " + delayTicks + " tick" +
                        (delayTicks == 1 ? "" : "s") + " delay before command "
                        + (index + 1) + " in schedule '" + name + "'. (" +
                        String.format("%.1f", delayTicks / 20.0) + " seconds)");
                  } else {
                    Messager.sendDanger(sender, "No schedule with name '" + name
                            + "' found or invalid command index.");
                  }
                }),
            new CommandAPICommand("remove-delay")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Remove a delay from a command in a schedule.",
                    "Remove a delay from a command in a schedule.")
                .withArguments(new StringArgument("name")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(info -> CommandScheduler.getInstance().getSchedules().stream()
                            .map(Schedule::getName)
                            .toArray(String[]::new))))
                .withArguments(new IntegerArgument("index", 1))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  int index = (int) args.get("index") - 1; // Convert to 0-based index

                  if (CommandScheduler.getInstance().removeDelay(name, index)) {
                    Messager.send(sender,"Removed delay from command " + (index + 1)
                        + " in schedule '" + name + "'.");
                  } else {
                    Messager.sendDanger(sender, "No schedule with name '" + name
                            + "' found or no delay was set for this command.");
                  }
                }));
  }

  @NotNull
  private static String getTypeInfo(@NotNull Schedule schedule) {
    String typeInfo;

    switch (schedule.getType()) {
      case INTERVAL -> typeInfo = "every " + ChatColor.GREEN + schedule.getIntervalMinutes()
          + ChatColor.WHITE + " minutes";
      case DAILY -> typeInfo = "daily at " + ChatColor.GREEN + schedule.getDailyTime();
      case CRON ->
        typeInfo = "using cron pattern " + ChatColor.GREEN + schedule.getCronPattern().getPattern();
      default -> typeInfo = "unknown schedule type";
    }
    return typeInfo;
  }
}
