package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Sandbox;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {

        // Enable flight
        if (event.getPlayer().hasPermission("dreamvisitor.fly")) {
            event.getPlayer().setAllowFlight(true);
        }

        // Send join messages
        String chatMessage = "**" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(event.getPlayer().getName())) + " joined the game**";
        try {
            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
        } catch (InsufficientPermissionException e) {
            Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
        }
        Bot.sendLog(chatMessage);

        PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

        if (memory.sandbox) {
            boolean sandboxerOnline = false;
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    sandboxerOnline = true;
                    onlinePlayer.sendMessage(Dreamvisitor.PREFIX + event.getPlayer().getName() + " is currently in sandbox mode.");
                }
            }
            if (!sandboxerOnline) Sandbox.disableSandbox(event.getPlayer());
        }

        if (Dreamvisitor.sillyMode) {
            // Set player skin to Bog if in silly mode
            PlayerUtility.setSkin(event.getPlayer(), UUID.fromString("eedf3c55-2e73-4e73-99a7-81d953745f0a"));
        }

    }
    
}
