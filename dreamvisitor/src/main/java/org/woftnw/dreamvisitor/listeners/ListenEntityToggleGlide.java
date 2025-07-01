package org.woftnw.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.jetbrains.annotations.NotNull;

public class ListenEntityToggleGlide implements Listener {

    @EventHandler
    public void onEntityToggleGlide(@NotNull EntityToggleGlideEvent event) {
        // This has something to do with flight, but I don't remember
        if (!event.isGliding() && !event.getEntity().isOnGround()) {
            event.setCancelled(true);
        }

    }

}
