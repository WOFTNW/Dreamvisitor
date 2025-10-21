package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Flight;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ListenPlayerToggleFlightEvent implements Listener {

    private static final HashMap<Player, BukkitTask> wingFlapSoundTask = new HashMap<>();

    @EventHandler
    public void onToggleFlight(@NotNull PlayerToggleFlightEvent event) {

        Player player = event.getPlayer();
        if (!Flight.inFlightGameMode(player)) {
            if (event.isFlying()) {
                player.setGliding(false);
                player.setFlySpeed(0.05f);
                player.setFlying(true);

                wingFlapSoundTask.put(player, Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {
                    if (!player.isOnline() || !player.isFlying()) {
                        // remove this task
                        cancelWingFlapSoundTask(player);

                    } else {
                        Flight.playWingFlapSound(player);
                    }
                }, 1, 15));


            } else {
                player.setFlying(false);
                PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
                if (!memory.flightDisabled) player.setGliding(true);
            }
        } else {
            if (event.isFlying()) {
                player.setFlySpeed(0.1f);
                player.setFlying(true);
                player.setGliding(false);
            } else {
                player.setFlying(false);
            }
        }


    }

    private static void cancelWingFlapSoundTask(Player player) {
        wingFlapSoundTask.get(player).cancel();
    }

}
