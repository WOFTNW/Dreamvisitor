package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerMemory;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ListenPlayerQuit implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // TODO: Send player quits to Discord
//        String chatMessage = "**" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getName())) + " left the game**";
//        Bot.getGameChatChannel().sendMessage(chatMessage).queue();
//        Bot.sendLog(chatMessage);

        final DVUser user = PlayerUtility.getUser(player);

        // Notify moderators if the quitting player is in Sandbox Mode
        if (user.isInSandboxMode()) {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    Messager.send(onlinePlayer, event.getPlayer().getName() + " left while in sandbox mode.");
                }
            }
        }

        // Save and clear player memory
        PlayerUtility.saveUser(user);

        Messager.debug("Checking sandbox.");

        // This player might be the last sandbox moderator, so check that
        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {

            Messager.debug("Task start.");

            // Check for sandboxed players
            boolean moderatorOnline = false;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Messager.debug("Is " + onlinePlayer.getName() + " moderator?");
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    Messager.debug("Yes! All good.");
                    moderatorOnline = true;
                    break;
                }
            }
            if (!moderatorOnline) {
                Messager.debug("No mods online! Gotta disable sandboxed.");
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Messager.debug("Is " + onlinePlayer + " sandboxed?");
                    if (PlayerUtility.getUser(onlinePlayer).isInSandboxMode()) {
                        Messager.debug("Yes. Disabling.");
                        Sandbox.disableSandbox(onlinePlayer);
                        onlinePlayer.sendMessage("You are no longer in Sandbox Mode because there are no sandbox managers available.");
                    }
                }
            }
        });



    }

}
