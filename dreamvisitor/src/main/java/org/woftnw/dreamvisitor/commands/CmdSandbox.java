package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.Sandbox;
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

                        Component message = Component.text("Players currently sandboxed:\n");

                        final HoverEvent<Component> tooltip = HoverEvent.showText(Component.text("Click to remove."));

                        for (Player player : sandboxedPlayers) {
                            final Location location = player.getLocation();
                            message = message.append(Component.text(player.getName(), NamedTextColor.YELLOW))
                                    .append(Component.text(" [", NamedTextColor.WHITE))
                                    .append(Component.text("Remove", NamedTextColor.RED))
                                    .hoverEvent(tooltip).clickEvent(ClickEvent.runCommand("/sandbox " + player.getName() + " false"))
                                    .append(Component.text("]\n", NamedTextColor.WHITE))
                                    .append(Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()))
                                    .append(Component.text(" in world " + Objects.requireNonNull(location.getWorld()).getName() + ".\n\n"));
                        }

                        Messager.send(sender, message);
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
