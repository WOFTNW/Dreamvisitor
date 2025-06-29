package org.woftnw.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.jetbrains.annotations.NotNull;

public class ListenEntityToggleGlideEvent implements Listener {

    @EventHandler
    public void onEntityToggleGlide(@NotNull EntityToggleGlideEvent event) {

        if (!event.isGliding() && !event.getEntity().isOnGround()) {
            event.setCancelled(true);
        }

    }

}
