package org.woftnw.dreamvisitor.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;

import java.util.Objects;

public class ListenPlayerInteractEntity implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        DVUser user = PlayerUtility.getUser(player);
        if (!user.isInSandboxMode()) return;
        if (event.getRightClicked() instanceof ItemFrame) {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    Messager.send(onlinePlayer, event.getPlayer().getName() + " interacted with an item frame with held item " + Objects.requireNonNull(player.getInventory().getItem(event.getHand())).getType());
                }
            }
        }
    }

}
