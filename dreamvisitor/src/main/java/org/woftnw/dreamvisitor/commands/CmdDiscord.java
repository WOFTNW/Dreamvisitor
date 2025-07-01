package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdDiscord implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("discord")
                .withHelp("Toggles Discord message visibility.", "Toggle whether messages from the Discord chat bridge appear in your chat.")
                .executesNative(((sender, args) -> {
                    final CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {

                        final DVUser user = PlayerUtility.getUser(player);

                        user.setShowDiscordMessages(!user.isShowDiscordOn());

                        Messager.send(player, "Discord visibility toggled to " + user.isShowDiscordOn() + ".");

                        PlayerUtility.saveUser(user);

                    } else throw CommandAPI.failWithString("This command must be executed as a player!");


                }));
    }
}
