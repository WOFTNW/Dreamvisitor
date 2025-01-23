package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.comms.DataSender;
import io.github.stonley890.dreamvisitor.data.*;
import io.github.stonley890.dreamvisitor.functions.SystemMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CmdTribeUpdate implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("tribeupdate")
                .withHelp("Update a player's tribe.", "Update the roles of a player based on their tribe.")
                .withPermission(CommandPermission.fromString("dreamvisitor.tribeupdate"))
                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("players");
                    assert players != null;

                    // This may take some time
                    if (sender instanceof Player) sender.sendMessage(SystemMessage.formatPrivate("Please wait..."));

                    // Run async
                    Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> {

                        List<String> tribeRoles = Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles");

                        for (Player player : players) {

                            UUID uuid = player.getUniqueId();

                            Dreamvisitor.debug(player.getUniqueId().toString());

                            PlayerTribe.updateTribeOfPlayer(uuid);

                            // Get tribe
                            Tribe playerTribe = PlayerTribe.getTribeOfPlayer(uuid);

                            DataSender.sendPlayerTribe(uuid, playerTribe);
                        }

                        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> sender.sendMessage(SystemMessage.formatPrivate("Updated " + players.size() + " player(s).")));

                    });
                });
    }

}
