package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.Sandbox;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CmdSandbox implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("sandbox")
                .withHelp("Manage the sandbox.", "Manage players' access to Sandbox Mode.")
                .withPermission(CommandPermission.fromString("dreamvisitor.sandbox"))
                .withOptionalArguments(new EntitySelectorArgument.ManyPlayers("players"))
                .withOptionalArguments(new BooleanArgument("state"))
                .executesNative((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("players");
                    if (players == null) {
                        final List<Player> sandboxedPlayers = new ArrayList<>();

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            final DVUser user = PlayerUtility.getUser(player);
                            if (user.isInSandboxMode()) sandboxedPlayers.add(player);
                        }

                        if (sandboxedPlayers.isEmpty()) {
                            Messager.send(sender, "No players currently online are in sandbox mode. Use /sandbox <player> [true|false] to toggle sandbox mode.");
                        }

                        final ComponentBuilder messageBuilder = new ComponentBuilder("Players currently sandboxed:\n");

                        final HoverEvent tooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to remove."));

                        for (Player player : sandboxedPlayers) {
                            final Location location = player.getLocation();
                            messageBuilder.append(player.getName()).color(ChatColor.YELLOW)
                                    .append(" [").color(ChatColor.WHITE)
                                    .append("Remove").color(ChatColor.RED)
                                    .event(tooltip).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sandbox " + player.getName() + " false"))
                                    .append("]\n").color(ChatColor.WHITE).event((ClickEvent) null)
                                    .append(String.valueOf(location.getBlockX())).append(", ").append(String.valueOf(location.getBlockY())).append(", ").append(String.valueOf(location.getBlockZ()))
                                    .append(" in world ").append(Objects.requireNonNull(location.getWorld()).getName()).append(".\n\n");
                        }

                        Messager.send(sender, messageBuilder.create());
                    } else {
                        final Object stateArg = args.get("state");
                        if (stateArg == null) {
                            players.forEach(player -> {
                                if (PlayerUtility.getUser(player).isInSandboxMode()) Sandbox.disableSandbox(player);
                                else Sandbox.enableSandbox(player);
                            });
                            Messager.send(sender, "Toggled sandbox mode for " + Messager.nameOrCountPlayer(players) + ".");
                        } else {
                            boolean sandboxState = (boolean) stateArg;
                            if (sandboxState) {
                                players.forEach(Sandbox::enableSandbox);
                                Messager.send(sender, "Enabled sandbox mode for " + Messager.nameOrCountPlayer(players) + ".");
                            } else {
                                players.forEach(Sandbox::disableSandbox);
                                Messager.send(sender, "Disabled sandbox mode for " + Messager.nameOrCountPlayer(players) + ".");
                            }
                        }
                    }
                });
    }
}
