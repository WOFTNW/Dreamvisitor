package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CmdUser implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("user")
                .withPermission(CommandPermission.fromString("dreamvisitor.user"))
                .withHelp("Get details of a player.", "Get details of a player, online or offline.")
                .withArguments(new OfflinePlayerArgument("player"))
                .executesNative((sender, args) -> {

                    OfflinePlayer player = (OfflinePlayer) args.get("player");

                    if (player == null) throw CommandAPI.failWithString("Player not found!");

                    String discordID;
                    String discordUsername = "N/A";
                    long discord;

                    // Discord ID from AccountLink.yml
                    try {
                        discord = AccountLink.getDiscordId(player.getUniqueId());
                        discordID = String.valueOf(discord);
                        discordUsername = Bot.getJda().retrieveUserById(discord).complete().getName();
                    } catch (NullPointerException e) {
                        discordID = "N/A";
                        // Discord username from JDA

                    }

                    Messager.send(sender, "Local data for player " + player.getName() + ":" +
                            "\nUUID: " + player.getUniqueId() +
                            "\nDiscord Username: " + discordUsername +
                            "\nDiscord ID: " + discordID
                    );
                });
    }
}
