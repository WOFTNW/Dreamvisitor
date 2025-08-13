package org.woftnw.dreamvisitor.listeners;

import com.earth2me.essentials.Essentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.functions.Messager;

import java.util.Arrays;
import java.util.List;

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
        final @NotNull List<Component> lines = event.lines();
        final String[] plainLines = (String[]) lines.stream().map(line -> PlainTextComponentSerializer.plainText().serialize(line)).toArray();

        // If the sign is empty, ignore
        boolean empty = true;
        for (String line : plainLines) {
            if (!line.isEmpty()) {
                empty = false;
                break;
            }
        }
        if (empty) return;

        // Create report message
        final Component message = Component.text(editor.getName() + " placed or edited a sign at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " in " + block.getWorld().getName() + ":\n"
                + plainLines[0] + "\n" + plainLines[1] + "\n" + plainLines[2] + "\n" + plainLines[3] + "\n");

        // Send to social spies
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ess.getUser(player).isSocialSpyEnabled())
                Messager.send(player, message);
        }

        // Send to log
        Dreamvisitor.getPlugin().getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
    }

}
