package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.functions.Flight;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerRespawn implements Listener {

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Messager.debug("Respawn");
        // Reset energy
        Flight.energy.put(event.getPlayer(), Flight.energyCapacity);
        Flight.setPlayerDepleted(event.getPlayer(), false);
        // Flight is disabled after respawn, so it needs to be re-enabled.
        Flight.setupFlight(event.getPlayer());

    }
}
