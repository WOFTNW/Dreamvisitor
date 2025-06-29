package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.functions.Flight;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerChangedWorld implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(@NotNull PlayerChangedWorldEvent event) {
        Flight.setupFlight(event.getPlayer());
    }

}
