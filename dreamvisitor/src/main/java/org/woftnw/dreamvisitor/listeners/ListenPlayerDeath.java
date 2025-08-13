package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.functions.Mail;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import org.jetbrains.annotations.NotNull;

public class ListenPlayerDeath implements Listener {
    
    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {

        Player player = event.getEntity().getPlayer();
        if (player != null && Mail.isPLayerDeliverer(player)) Mail.cancel(player);

        if (event.deathMessage() == null) return;

        // TODO: Send death messages
//        String chatMessage = "**" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(event.getDeathMessage())) + "**";
//        try {
//            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
//        } catch (InsufficientPermissionException e) {
//            Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
//        }
//        Bot.sendLog(chatMessage);
    }
    
}
