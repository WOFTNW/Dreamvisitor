package org.woftnw.dreamvisitor.listeners;

import org.woftnw.dreamvisitor.data.Config;
import org.woftnw.dreamvisitor.functions.Flight;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ListenPlayerMoveEvent implements Listener {

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {

        Player player = event.getPlayer();
        if (event.getTo() == null) return;

        if (player.isFlying() && !Flight.inFlightGameMode(player)) {
            // Remove energy if flying
            try {
                Double energy = Flight.energy.get(player);

                // Return if worlds are different
                if (!Objects.equals(event.getTo().getWorld(), event.getFrom().getWorld())) return;

                // Calculate energy to remove
                double energyToRemove = getEnergyToRemove(event);

                energy -= energyToRemove;

                if (energy < 0) energy = 0.0;
                Flight.energy.put(player, energy);
            } catch (NullPointerException e) {
                Flight.energy.put(player, Flight.energyCapacity);
            }
        }
    }

    private static double getEnergyToRemove(@NotNull PlayerMoveEvent event) {
        double energyToRemove;
        Location from2d = event.getFrom().clone();
        from2d.setY(0);
        Location to2d = Objects.requireNonNull(event.getTo()).clone();
        to2d.setY(0);

        double distance2d = from2d.distance(to2d);
        distance2d = Math.abs(distance2d);

        double fromY = event.getFrom().getY();
        double toY = event.getTo().getY();

        double distanceY = toY - fromY;
        if (distanceY < 0) distanceY = 0;

        double flightEnergyDepletionXYMultiplier = Config.getDouble("flightEnergyDepletionXYMultiplier", 1.0);
        double flightEnergyDepletionYMultiplier = Config.getDouble("flightEnergyDepletionYMultiplier", 2.00);
        energyToRemove = distance2d * flightEnergyDepletionXYMultiplier + distanceY * flightEnergyDepletionYMultiplier;
        return energyToRemove;
    }

}
