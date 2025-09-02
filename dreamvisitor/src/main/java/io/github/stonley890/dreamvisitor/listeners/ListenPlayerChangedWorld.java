package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.functions.Flight;
import io.github.stonley890.dreamvisitor.functions.Mail;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerChangedWorld implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(@NotNull PlayerChangedWorldEvent event) {
        if (Mail.isPLayerDeliverer(event.getPlayer())) {
            // Cancel parcel if going to nether
            if (event.getPlayer().getWorld().isUltraWarm()) Mail.cancel(event.getPlayer());
        }
        Flight.setupFlight(event.getPlayer());
    }

}
