package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListenTimeSkip implements Listener {

    @EventHandler
    public void onTimeSkipEvent(@NotNull TimeSkipEvent event) {
        // Sync time skips
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP && !event.isCancelled()) {
            List<World> worlds = Bukkit.getWorlds();
            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
                for (World world : worlds) {
                    world.setTime(event.getWorld().getTime());
                }
            });
        }
    }

}
