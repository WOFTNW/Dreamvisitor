package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPausechat implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("pausechat")
                .withPermission(CommandPermission.fromString("dreamvisitor.pausechat"))
                .withHelp("Pause the chat.", "Suppresses messages from players and the Discord chat bridge.")
                .executesNative((sender, args) -> {
                    if (Dreamvisitor.chatPaused) {

                        // Change settings
                        Dreamvisitor.chatPaused = false;
                        plugin.getConfig().set("chatPaused", Dreamvisitor.chatPaused);

                        // Broadcast to server
                        Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Chat has been unpaused.");

                        // TODO: Broadcast to chat channel
//                        Bot.getGameChatChannel().sendMessage("**Chat has been unpaused. Messages will now be sent to Minecraft**").queue();

                    } else {

                        // Change settings
                        Dreamvisitor.chatPaused = true;
                        plugin.getConfig().set("chatPaused", Dreamvisitor.chatPaused);

                        // Broadcast to server
                        Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Chat has been paused.");

                        // TODO: Broadcast to chat channel
//                        Bot.getGameChatChannel().sendMessage("**Chat has been paused. Messages will not be sent to Minecraft**").queue();

                    }
                    plugin.saveConfig();
                });
    }
}
