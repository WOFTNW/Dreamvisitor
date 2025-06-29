package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerMemory;
import org.woftnw.dreamvisitor.data.PlayerTribe;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.Tribe;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.Sandbox;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {

        final Player player = event.getPlayer();

        // Edit player list name if staff
        final User lpUser = Dreamvisitor.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (lpUser != null) {

            // Get the player's tribe
            Tribe tribeOfPlayer = PlayerTribe.getTribeOfPlayer(player.getUniqueId());
            // Create a variable that represents the color to set the player's name to
            ChatColor tribeColor;
            // If the player has a tribe, set it to tribe color
            // If not, set to white.
            if (tribeOfPlayer != null) {
                tribeColor = tribeOfPlayer.getColor();
            } else {
                tribeColor = ChatColor.WHITE;
            }

            String prefix = lpUser.getCachedData().getMetaData().getPrefix();
            if (prefix != null) player.setPlayerListName(prefix.replace('&', '§') + tribeColor + player.getName());
        }

        // Enable flight
        if (player.hasPermission("dreamvisitor.fly")) {
            player.setAllowFlight(true);
        }

        // TODO: Send join messages
//        String chatMessage = "**" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getName())) + " joined the game**";
//        try {
//            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
//        } catch (InsufficientPermissionException e) {
//            Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
//        }
//        Bot.sendLog(chatMessage);

        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        if (memory.sandbox) {
            boolean sandboxerOnline = false;
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    sandboxerOnline = true;
                    Messager.send(onlinePlayer, player.getName() + " is currently in sandbox mode.");
                }
            }
            if (!sandboxerOnline) Sandbox.disableSandbox(player);
        }

    }
    
}
