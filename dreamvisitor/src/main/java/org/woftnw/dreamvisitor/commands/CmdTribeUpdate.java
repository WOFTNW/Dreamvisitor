package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.woftnw.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.*;
import org.woftnw.dreamvisitor.data.PlayerTribe;
import org.woftnw.dreamvisitor.data.Tribe;
import org.woftnw.dreamvisitor.functions.Messager;
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
                    if (sender instanceof Player) Messager.send(sender, "Please wait...");

                    // Run async
                    Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> {

                        for (Player player : players) {

                            UUID uuid = player.getUniqueId();

                            PlayerTribe.updateTribeOfPlayer(uuid);

                            // Get tribe
                            Tribe playerTribe = PlayerTribe.getTribeOfPlayer(uuid);

                            if (playerTribe != null) {

                                // Update LP groups
                                Messager.debug("Updating permissions");
                                PlayerTribe.updatePermissions(uuid);

                            }
                        }

                        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> Messager.send(sender, "Updated " + Messager.nameOrCountPlayer(players) + "."));

                    });
                });
    }

}
