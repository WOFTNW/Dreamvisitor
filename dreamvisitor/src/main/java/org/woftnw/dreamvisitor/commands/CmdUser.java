package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;
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

                    final OfflinePlayer player = (OfflinePlayer) args.get("player");

                    if (player == null) throw CommandAPI.failWithString("Player not found!");

                    String discordID = "N/A";
                    String discordUsername = "N/A";

                    final DVUser user = PlayerUtility.getUser(player.getUniqueId());
                    if (user.getDiscordId() == null) {
                        discordID = String.valueOf(user.getDiscordId());
                        discordUsername = user.getDiscordUsername();
                    }

                    Messager.send(sender, "Local data for player " + player.getName() + ":" +
                            "\nUUID: " + player.getUniqueId() +
                            "\nDiscord Username: " + discordUsername +
                            "\nDiscord ID: " + discordID
                    );
                });
    }
}
