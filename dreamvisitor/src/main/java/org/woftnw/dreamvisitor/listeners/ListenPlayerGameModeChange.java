package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.data.PlayerMemory;
import org.woftnw.dreamvisitor.data.PlayerUtility;
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

        Player player = event.getPlayer();
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        Flight.setupFlight(player);

        if (memory.autoinvswap && ((player.getGameMode().equals(GameMode.SURVIVAL) && event.getNewGameMode().equals(GameMode.CREATIVE)) || (player.getGameMode().equals(GameMode.CREATIVE) && event.getNewGameMode().equals(GameMode.SURVIVAL)))) Bukkit.dispatchCommand(player, "invswap");

    }

}
