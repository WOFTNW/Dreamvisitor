package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.*;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
                    if (sender instanceof Player) sender.sendMessage(Dreamvisitor.PREFIX + "Please wait...");

                    // Run async
                    Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> {

                        for (Player player : players) {

                            UUID uuid = player.getUniqueId();

                            try {
                                PlayerTribe.updateTribeOfPlayer(uuid);
                            } catch (NullPointerException e) {
                                Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> sender.sendMessage(Dreamvisitor.PREFIX + player.getName() + " does not have a tag or team associated with a tribe."));
                            }

                            // Get tribe
                            Tribe playerTribe = PlayerTribe.getTribeOfPlayer(uuid);

                            if (playerTribe != null) {

                                // Update LP groups
                                Dreamvisitor.debug("Updating permissions");
                                PlayerTribe.updatePermissions(uuid);

                            }
                        }

                        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> sender.sendMessage(Dreamvisitor.PREFIX + "Updated " + players.size() + " players."));

                    });
                });
    }

}
