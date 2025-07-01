package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;

public class ListenServerPing implements Listener {

    @EventHandler
    public void onPing(@NotNull ServerListPingEvent event) {
        // Show player limit override
        event.setMaxPlayers(Dreamvisitor.getPlugin().getServer().getMaxPlayers());

        // Show custom MOTD
        if (Dreamvisitor.MOTD != null) event.setMotd(Dreamvisitor.MOTD);
    }

}
