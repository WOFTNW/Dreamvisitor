package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPlayerlimit implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("playerlimit")
                .withPermission(CommandPermission.fromString("dreamvisitor.playerlimit"))
                .withHelp("Set the player limit.", "Override the server player limit.")
                .withOptionalArguments(new IntegerArgument("newLimit", -1))
                .executesNative((sender, args) -> {

                    Object newLimitArg = args.get("newLimit");
                    if (newLimitArg == null) {
                        Messager.send(sender, "Player limit override is currently set to " + Dreamvisitor.playerLimit + ".");
                    } else {
                        try {
                            // Change config
                            final int result = (int) newLimitArg;
                            // Dreamvisitor.getPlugin().getServer().setMaxPlayers(result);

                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                if (player.isOp()) {
                                    player.sendMessage(ChatColor.BLUE + "Player limit override set to " + result);
                                }
                            }

                            Dreamvisitor.playerLimit = result;
                            plugin.getConfig().set("playerlimit", result);
                            plugin.saveConfig();

                        } catch (NumberFormatException e) {
                            Messager.sendDanger(sender, "Incorrect arguments! /playerlimit <number of players (set -1 to disable)>");
                        }
                    }

                });
    }
}
