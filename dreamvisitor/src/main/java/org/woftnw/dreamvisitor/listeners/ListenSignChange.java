package org.woftnw.dreamvisitor.listeners;

import com.earth2me.essentials.Essentials;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.Dreamvisitor;

public class ListenSignChange implements Listener {

    @EventHandler
    public void onSignChangeEvent(@NotNull SignChangeEvent event) {
        if (event.isCancelled()) return;

        // Get essentials API
        final Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) return;
        final Player editor = event.getPlayer();
        if (editor.isOp()) return;

        final Block block = event.getBlock();
        final String[] lines = event.getLines();

        // If the sign is empty, ignore
        boolean empty = true;
        for (String line : lines) {
            if (!line.isBlank()) {
                empty = false;
                break;
            }
        }
        if (empty) return;

        // Create report message
        final String message = ChatColor.GOLD + editor.getName() + " placed or edited a sign at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " in " + block.getWorld().getName() + ":\n" + ChatColor.RESET
                + lines[0] + "\n" + lines[1] + "\n" + lines[2] + "\n" + lines[3] + "\n";

        // Send to social spies
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ess.getUser(player).isSocialSpyEnabled())
                player.sendMessage(message);
        }

        // Send to log
        Dreamvisitor.getPlugin().getLogger().info(ChatColor.stripColor(message));
    }

}
