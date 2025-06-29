package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.data.Config;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class ListenEntityDamage implements Listener {
    
    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @EventHandler
    public void onEntityDamageEvent(@NotNull EntityDamageByEntityEvent event) {

        // If PvP is disabled and the damage type is player, cancel the event
        if ((event.getDamager().getType() == EntityType.PLAYER && event.getEntity().getType() == EntityType.PLAYER) && Config.getBoolean("disable_pvp", false)) {
                event.setCancelled(true);
        }
    }
    
}
