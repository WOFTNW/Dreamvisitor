package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Flight;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerGameModeChange implements Listener {

    @EventHandler
    public void onPlayerGameModeChangeEvent(@NotNull PlayerGameModeChangeEvent event) {

        final Player player = event.getPlayer();
        final DVUser user = PlayerUtility.getUser(player);

        // Setup flight on game mode change
        Flight.setupFlight(player);

        // If auto inventory swap is on and player changes from survival to creative or vise versa, swap inventory
        if (user.isAutoInvSwapEnabled() && ((player.getGameMode().equals(GameMode.SURVIVAL) && event.getNewGameMode().equals(GameMode.CREATIVE)) || (player.getGameMode().equals(GameMode.CREATIVE) && event.getNewGameMode().equals(GameMode.SURVIVAL)))) Bukkit.dispatchCommand(player, "invswap");

    }

}
