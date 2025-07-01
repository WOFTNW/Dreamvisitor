package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.PauseBypass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CmdPauseBypass implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("pausebypass")
                .withHelp("Allow players to bypass chat pause.", "Allow players to chat even when chat is paused.")
                .withPermission(CommandPermission.fromString("dreamvisitor.pausechat"))
                .withSubcommand(new CommandAPICommand("add")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executesNative((sender, args) -> {
                            final Collection<Player> players = (Collection<Player>) args.get("players");
                            final List<UUID> playersList = PauseBypass.getPlayers();
                            assert players != null;
                            playersList.addAll(players.stream().map(Player::getUniqueId).toList());
                            PauseBypass.setPlayers(playersList);
                            Messager.send(sender, "Added " + Messager.nameOrCountPlayer(players) + " to the bypass list.");
                        })
                )
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executesNative((sender, args) -> {
                            final Collection<Player> players = (Collection<Player>) args.get("players");
                            final List<UUID> playersList = PauseBypass.getPlayers();
                            assert players != null;
                            final boolean removed = playersList.removeAll(players.stream().map(Player::getUniqueId).toList());
                            PauseBypass.setPlayers(playersList);
                            if (removed) Messager.send(sender, "Removed " + Messager.nameOrCountPlayer(players) + " from the bypass list.");
                            else throw CommandAPI.failWithString("No players were removed.");
                        })
                )
                .withSubcommand(new CommandAPICommand("list")
                        .executesNative((sender, args) -> {
                            // Build list
                            final StringBuilder list = new StringBuilder();

                            for (UUID player : PauseBypass.getPlayers()) {
                                if (!list.isEmpty()) list.append(", ");
                                list.append(PlayerUtility.getUsernameOfUuid(player));
                            }
                            Messager.send(sender, "Players bypassing: " + list);
                        })
                );
    }
}
