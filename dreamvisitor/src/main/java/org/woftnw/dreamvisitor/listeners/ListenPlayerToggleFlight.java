package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.Dreamvisitor;
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
import java.util.Objects;

public class ListenPlayerToggleFlight implements Listener {

    private static final HashMap<Player, BukkitTask> wingFlapSoundTask = new HashMap<>();

    @EventHandler
    public void onToggleFlight(@NotNull PlayerToggleFlightEvent event) {

        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            // If player is in survival or adventure
            if (event.isFlying()) {
                // If the player is going into flight mode
                // Set player states
                player.setGliding(false);
                player.setFlySpeed(0.05f);
                player.setFlying(true);

                // Start flapping sounds
                wingFlapSoundTask.put(player, Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {
                    if (!player.isOnline() || !player.isFlying()) {
                        // If the player is offline or not flying, cancel future flap sounds.
                        cancelWingFlapSoundTask(player);

                    } else {
                        // Otherwise, play it!
                        Objects.requireNonNull(player.getLocation().getWorld()).playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.4f, 1.2f);
                    }
                }, 1, 15));


            } else {
                // If player is exiting flight, put into glide mode
                player.setFlying(false);
                player.setGliding(true);
            }
        } else {
            // If player is in Creative or Spectator
            if (event.isFlying()) {
                // Set speed accordingly
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
