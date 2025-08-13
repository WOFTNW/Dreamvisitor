package org.woftnw.dreamvisitor.listeners;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.PlayerTribe;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.Tribe;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Flight;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.functions.Sandbox;
import net.luckperms.api.model.user.User;
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
            TextColor tribeColor;
            // If the player has a tribe, set it to tribe color
            // If not, set to white.
            if (tribeOfPlayer != null) {
                tribeColor = tribeOfPlayer.getColor();
            } else {
                tribeColor = NamedTextColor.WHITE;
            }

            // Get the prefix from LuckPerms data
            String prefix = lpUser.getCachedData().getMetaData().getPrefix();
            // Set the player list name with the prefix
            if (prefix != null) player.playerListName(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix.replace('&', '§') + tribeColor + player.getName()));
        }

        // Enable flight
        Flight.setupFlight(player);

        // TODO: Send join messages
//        String chatMessage = "**" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getName())) + " joined the game**";
//        try {
//            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
//        } catch (InsufficientPermissionException e) {
//            Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
//        }
//        Bot.sendLog(chatMessage);

        DVUser user = PlayerUtility.getUser(player);

        // If the player is in sandbox mode, handle accordingly
        if (user.isInSandboxMode()) {
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
