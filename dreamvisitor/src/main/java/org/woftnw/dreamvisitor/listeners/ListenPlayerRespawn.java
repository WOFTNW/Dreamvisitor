package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.functions.Flight;
import org.woftnw.dreamvisitor.functions.Messager;
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
