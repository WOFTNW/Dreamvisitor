package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerMemory;
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

        PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

        if (memory.sandbox) {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    Messager.send(onlinePlayer, event.getPlayer().getName() + " left while in sandbox mode.");
                }
            }
        }

        try {
            PlayerUtility.savePlayerMemory(player.getUniqueId());
            PlayerUtility.clearPlayerMemory(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save player memory! Does the server have write access? Player memory will remain in memory. " + e.getMessage());
        }

        Messager.debug("Checking sandbox.");

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
                    if (PlayerUtility.getPlayerMemory(onlinePlayer.getUniqueId()).sandbox) {
                        Messager.debug("Yes. Disabling.");
                        Sandbox.disableSandbox(onlinePlayer);
                        onlinePlayer.sendMessage("You are no longer in Sandbox Mode because there are no sandbox managers available.");
                    }
                }
            }
        });



    }

}
