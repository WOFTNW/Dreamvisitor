package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Flight {
    public static double energyCapacity = Dreamvisitor.getPlugin().getConfig().getInt("flightEnergyCapacity");
    public static double reactivationPoint = Dreamvisitor.getPlugin().getConfig().getInt("flightRegenerationPoint");
    public static final Map<Player, Double> energy = new HashMap<>();
    private static final Map<Player, Boolean> energyDepletion = new HashMap<>();
    private static final Map<Player, Boolean> flightRestricted = new HashMap<>();
    private static final Map<Player, Vector> lastPosition = new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                setupFlight(player);

                if (isGonnaTouchGround(player)) player.setAllowFlight(false);

                energy.putIfAbsent(player, energyCapacity);

                // Get bossbar (even if it's null)
                NamespacedKey namespacedKey = NamespacedKey.fromString("dreamvisitor:" + player.getUniqueId().toString().toLowerCase() + "-energy", Dreamvisitor.getPlugin());
                assert namespacedKey != null;
                KeyedBossBar bossBar = Bukkit.getBossBar(namespacedKey);

                if (energy.get(player) < energyCapacity) {

                    if (!player.isFlying() || inFlightGameMode(player)) {
                        // Regenerate energy if not flying or in creative/spectator mode
                        try {
                            energy.compute(player, (k, i) -> i + 1);
                        } catch (NullPointerException e) {
                            energy.put(player, energyCapacity);
                        }
                    }

                    if (energy.get(player) > energyCapacity) energy.put(player, energyCapacity);

                    if (bossBar == null) { // Create bossbar if it's null
                        bossBar = Bukkit.createBossBar(namespacedKey, "Energy", BarColor.GREEN, BarStyle.SEGMENTED_10);
                        bossBar.addPlayer(player);
                    }

                    bossBar.setVisible(!inFlightGameMode(player));

                    // Set progress
                    bossBar.setProgress(energy.get(player) / energyCapacity);

                    // Remove player from flight if energy runs out
                    if (energy.get(player) <= 0) {
                        // Set bossbar to red if it's depleted
                        bossBar.setColor(BarColor.RED);
                        setPlayerDepleted(player, true);
                        if (player.isFlying()) {
                            player.setFlying(false);
                            player.setGliding(true);
                            player.setAllowFlight(false);
                        }
                    }

                    // Set bossbar to green if it reaches reactivation point
                    if (isPlayerDepleted(player) && energy.get(player) >= reactivationPoint) {
                        bossBar.setColor(BarColor.GREEN);
                        setPlayerDepleted(player, false);
                        setupFlight(player);
                    }

                } else if (bossBar != null) {
                    // Remove bossbar if it's full
                    bossBar.removePlayer(player);
                    bossBar.setVisible(false);
                    Bukkit.removeBossBar(namespacedKey);
                }

                final Input input = player.getCurrentInput();

                if (player.isFlying() && !inFlightGameMode(player)) {
                    // Remove energy if flying
                    try {
                        Double energy = Flight.energy.get(player);
                        Vector lastLoc = lastPosition.get(player);
                        Vector currentLoc = player.getLocation().toVector();

                        // Check planar movement
                        double movement2d = 0;
                        // If pressing movement key, remove 1
                        if (input.isBackward() || input.isForward() || input.isLeft() || input.isRight()) movement2d = 1;
                        // If sprinting, multiply that by 2
                        if (player.isSprinting()) movement2d *= 2;
                        // If not actually moving, don't remove energy
                        if (lastLoc != null && Objects.equals(currentLoc.getX(), lastLoc.getX()) && Objects.equals(currentLoc.getZ(), lastLoc.getZ()))
                            movement2d = 0;

                        // Check vertical movement
                        double movementY = 0;
                        // If pressing jump, remove 1
                        if (input.isJump()) movementY = 1;
                        // If not actually moving up, don't remove energy
                        if (lastLoc != null && Objects.equals(currentLoc.getY(), lastLoc.getY())) movementY = 0;

                        // Get multiplication factors from config
                        final double flightEnergyDepletionXYMultiplier = Dreamvisitor.getPlugin().getConfig().getDouble("flightEnergyDepletionXYMultiplier");
                        final double flightEnergyDepletionYMultiplier = Dreamvisitor.getPlugin().getConfig().getDouble("flightEnergyDepletionYMultiplier");
                        // Calculate the total energy to remove
                        final double energyToRemove = movement2d * flightEnergyDepletionXYMultiplier + movementY * flightEnergyDepletionYMultiplier;

                        // Calculate what the player's energy should be
                        energy -= energyToRemove;

                        // Ensure energy is not below zero
                        if (energy < 0) energy = 0.0;
                        // Save new energy state to player
                        Flight.energy.put(player, energy);
                        Flight.lastPosition.put(player, currentLoc);
                    } catch (NullPointerException e) {
                        // If the energy for the player doesn't exist for some reason, set it to full
                        energy.put(player, energyCapacity);
                    }
                }
            }
        }, 0, 0);
    }

    /**
     * Check if a player is depleted (indicated red energy bar).
     * @param player the player to check.
     * @return true if the player is depleted, false otherwise
     */
    public static boolean isPlayerDepleted(Player player) {
        return (energyDepletion.computeIfAbsent(player, k -> false));
    }

    public static void setPlayerDepleted(Player player, boolean depleted) {
        energyDepletion.put(player, depleted);
    }

    /**
     * Whether flight is restricted by a WorldGuard region.
     *
     * @param player the player to check for
     * @return whether flying is permitted by the player's region
     */
    public static boolean isFlightRestricted(Player player) {
        return flightRestricted.computeIfAbsent(player, k -> false);
    }

    public static void setFlightRestricted(@NotNull Player player, boolean restricted) {
        flightRestricted.put(player, restricted);
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {

            if (restricted) {
                if (player.isFlying()) {
                    player.setFlying(false);
                    player.setGliding(true);
                }
                player.setAllowFlight(false);
            } else setupFlight(player);

        }

    }

    /**
     * Allow the player to fly if appropriate. This method will NOT allow flight if the player does not have permission,
     * is in a region where they can't fly, is depleted of energy, disabled flight themselves, or is about to touch the
     * ground.
     *
     * @param player the player to set up flight for.
     */
    public static void setupFlight(@NotNull Player player) {
        boolean alreadyAllowedFlight = player.getAllowFlight();
        if (alreadyAllowedFlight) return;
        boolean hasPermissionToFly = player.hasPermission("dreamvisitor.fly");
        if (!hasPermissionToFly) return;
        if (isFlightRestricted(player)) return;
        if (isPlayerDepleted(player)) return;
        boolean playerDisabledOwnFlight = PlayerUtility.getPlayerMemory(player.getUniqueId()).flightDisabled;
        if (playerDisabledOwnFlight) return;
        if (isGonnaTouchGround(player)) return;

        Dreamvisitor.debug("All requirements met for flight.");
        player.setAllowFlight(true);
//        Bukkit.getScheduler().runTaskLater(Dreamvisitor.getPlugin(), () -> player.setAllowFlight(true), 1);

    }

    /**
     * Whether a player is in a flight-enabled game mode like Creative or Spectator.
     *
     * @param player the player to check.
     * @return true if in Creative or Spectator, false otherwise.
     */
    public static boolean inFlightGameMode(@NotNull Player player) {
        return (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
    }


    /**
     * Whether the player will touch the ground on the next tick based on their current position and velocity.
     *
     * @param player the player to check
     * @return true if they will hit the ground, false otherwise
     */
    public static boolean isGonnaTouchGround(@NotNull Player player) {

        Vector velocity = player.getVelocity();
        double hitboxWidth = Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).getValue() * 0.6;
        Location location = player.getLocation();
        World world = player.getWorld();

        if (velocity.getY() >= -0.08) return false;

        Location point1 = location.clone().add((hitboxWidth / 2), 0, (hitboxWidth / 2));
        Location point2 = location.clone().add(-(hitboxWidth / 2), 0, (hitboxWidth / 2));
        Location point3 = location.clone().add((hitboxWidth / 2), 0, -(hitboxWidth / 2));
        Location point4 = location.clone().add(-(hitboxWidth / 2), 0, -(hitboxWidth / 2));

        RayTraceResult traceResult1 = world.rayTraceBlocks(point1, velocity, 1);
        RayTraceResult traceResult2 = world.rayTraceBlocks(point2, velocity, 1);
        RayTraceResult traceResult3 = world.rayTraceBlocks(point3, velocity, 1);
        RayTraceResult traceResult4 = world.rayTraceBlocks(point4, velocity, 1);

        boolean overlapsPoint1 = (traceResult1 != null);
        boolean overlapsPoint2 = (traceResult2 != null);
        boolean overlapsPoint3 = (traceResult3 != null);
        boolean overlapsPoint4 = (traceResult4 != null);

        return (overlapsPoint1 || overlapsPoint2 || overlapsPoint3 || overlapsPoint4);
    }
}
