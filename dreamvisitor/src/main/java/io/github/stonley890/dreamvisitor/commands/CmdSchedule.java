package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.CommandScheduler;
import io.github.stonley890.dreamvisitor.functions.CommandScheduler.Schedule;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CmdSchedule implements DVCommand {

  @NotNull
  @Override
  public CommandAPICommand getCommand() {
    return new CommandAPICommand("schedule")
        .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
        .withHelp("Schedule commands to run at specific intervals.", "Schedule commands to run at specific intervals.")
        .withSubcommands(
            new CommandAPICommand("add")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("Add a new scheduled command.", "Add a new scheduled command.")
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("interval-minutes"))
                .withArguments(new GreedyStringArgument("command"))
                .executesNative((sender, args) -> {
                  String name = (String) args.get("name");
                  int intervalMinutes = (int) args.get("interval-minutes");
                  String command = (String) args.get("command");

                  CommandScheduler.getInstance().addSchedule(name, intervalMinutes, command);
                  sender.sendMessage(Dreamvisitor.PREFIX + "Added schedule '" + name + "' to run '" + command +
                      "' every " + intervalMinutes + " minutes.");
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
                    sender.sendMessage(Dreamvisitor.PREFIX + "Removed schedule '" + name + "'.");
                  } else {
                    sender.sendMessage(
                        Dreamvisitor.PREFIX + ChatColor.RED + "No schedule with name '" + name + "' found.");
                  }
                }),
            new CommandAPICommand("list")
                .withPermission(CommandPermission.fromString("dreamvisitor.schedule"))
                .withHelp("List all scheduled commands.", "List all scheduled commands.")
                .executesNative((sender, args) -> {
                  List<Schedule> schedules = CommandScheduler.getInstance().getSchedules();
                  if (schedules.isEmpty()) {
                    sender.sendMessage(Dreamvisitor.PREFIX + "No schedules configured.");
                    return;
                  }

                  sender.sendMessage(Dreamvisitor.PREFIX + "Scheduled commands:");
                  for (Schedule schedule : schedules) {
                    sender.sendMessage(ChatColor.YELLOW + schedule.getName() + ChatColor.WHITE +
                        ": " + ChatColor.GRAY + "'" + schedule.getCommand() + "'" +
                        ChatColor.WHITE + " every " + ChatColor.GREEN +
                        schedule.getIntervalMinutes() + ChatColor.WHITE + " minutes. " +
                        ChatColor.AQUA + schedule.getTimeUntilNextRun() + ChatColor.WHITE + " until next run.");
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
                    sender.sendMessage(Dreamvisitor.PREFIX + "Running schedule '" + name + "' now.");
                  } else {
                    sender.sendMessage(
                        Dreamvisitor.PREFIX + ChatColor.RED + "No schedule with name '" + name + "' found.");
                  }
                }));
  }
}
