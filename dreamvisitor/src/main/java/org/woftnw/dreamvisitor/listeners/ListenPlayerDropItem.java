package org.woftnw.dreamvisitor.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;

public class ListenPlayerDropItem implements Listener {

    @EventHandler
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        DVUser user = PlayerUtility.getUser(player);
        if (user.isInSandboxMode()) event.setCancelled(true);
    }

}
