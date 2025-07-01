package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import org.woftnw.dreamvisitor.functions.Radio;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public class CmdRadio implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("radio")
                .withPermission(CommandPermission.fromString("dreamvisitor.radio"))
                .withHelp("Send a message using the radio.", "Send a message to all other players who can access the radio.")
                .withArguments(new GreedyStringArgument("message"))
                .executesNative((sender, args) -> {

                    final String message = (String) args.get("message");

                    final CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        Radio.buildMessage(message, player.getName(), getCommand().getName(), null);
                    } else if (callee instanceof ConsoleCommandSender) {
                        Radio.buildMessage(message, "Console", getCommand().getName(), null);
                    } else {
                        throw CommandAPI.failWithString("This command can only be executed by a player or the console!");
                    }
                });
    }
}
