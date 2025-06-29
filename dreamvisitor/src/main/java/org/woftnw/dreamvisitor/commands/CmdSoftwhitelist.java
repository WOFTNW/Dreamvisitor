package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.SoftWhitelist;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CmdSoftwhitelist implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("softwhitelist")
                .withPermission(CommandPermission.fromString("dreamvisitor.softwhitelist"))
                .withHelp("Manage the soft whitelist.", "Manage the soft whitelist.")
                .withSubcommand(new CommandAPICommand("add")
                        .withArguments(new OfflinePlayerArgument("player"))
                        .executesNative((sender, args) -> {
                            OfflinePlayer player = (OfflinePlayer) args.get("player");
                            if (player == null) throw CommandAPI.failWithString("That player could not be found!");
                            List<UUID> players = SoftWhitelist.getPlayers();
                            if (players.contains(player.getUniqueId())) throw CommandAPI.failWithString("That player is already on the soft whitelist!");
                            players.add(player.getUniqueId());
                            SoftWhitelist.setPlayers(players);
                            Messager.send(sender, "Added " + player.getName() + ".");
                        })
                )
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new OfflinePlayerArgument("player"))
                        .executesNative((sender, args) -> {
                            OfflinePlayer player = (OfflinePlayer) args.get("player");
                            if (player == null) throw CommandAPI.failWithString("That player could not be found!");
                            List<UUID> players = SoftWhitelist.getPlayers();
                            if (!players.contains(player.getUniqueId())) throw CommandAPI.failWithString("That player is not on soft whitelist!");
                            players.remove(player.getUniqueId());
                            SoftWhitelist.setPlayers(players);
                            Messager.send(sender,"Removed " + player.getName() + ".");
                        })
                )
                .withSubcommand(new CommandAPICommand("list")
                        .executesNative((sender, args) -> {
                            List<UUID> players = SoftWhitelist.getPlayers();
                            StringBuilder list = new StringBuilder();

                            for (UUID player : players) {
                                if (!list.isEmpty()) {
                                    list.append(", ");
                                }
                                list.append(PlayerUtility.getUsernameOfUuid(player));
                            }
                            Messager.send(sender, "Players soft-whitelisted: " + list);
                        })
                )
                .withSubcommand(new CommandAPICommand("on")
                        .executesNative((sender, args) -> {
                            plugin.getConfig().set("softwhitelist", true);
                            plugin.saveConfig();
                            Messager.send(sender,"Soft whitelist enabled.");
                        })
                )
                .withSubcommand(new CommandAPICommand("off")
                        .executesNative((sender, args) -> {
                            plugin.getConfig().set("softwhitelist", false);
                            plugin.saveConfig();
                            Messager.send(sender, "Soft whitelist disabled.");
                        })
                );
    }
}
