package org.woftnw.dreamvisitor.listeners;

import org.bukkit.block.Container;
import org.bukkit.block.DecoratedPot;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;

public class ListenPlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        DVUser user = PlayerUtility.getUser(player);
        if (!user.isInSandboxMode()) return;
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            assert event.getClickedBlock() != null;
            if ((event.getClickedBlock().getState() instanceof Container && !player.isSneaking())
                    || (event.getClickedBlock().getState() instanceof DecoratedPot)
                    || (event.getClickedBlock().getState() instanceof EnderChest)
                    || (event.getItem() != null && event.getItem().getItemMeta() instanceof SpawnEggMeta))
                event.setCancelled(true);
        }
    }


}
