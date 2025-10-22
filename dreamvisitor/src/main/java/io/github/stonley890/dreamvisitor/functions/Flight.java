package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

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
    private static final Map<Player, Integer> riseCooldown = new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), Flight::tick, 0, 0);
    }

    private static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            setupFlight(player);

            if (isGonnaTouchGround(player) && !inFlightGameMode(player)) player.setAllowFlight(false);

            energy.putIfAbsent(player, energyCapacity);

            // Get bossbar (even if it's null)
            NamespacedKey namespacedKey = NamespacedKey.fromString("dreamvisitor:" + player.getUniqueId().toString().toLowerCase() + "-energy", Dreamvisitor.getPlugin());
            assert namespacedKey != null;
            KeyedBossBar bossBar = Bukkit.getBossBar(namespacedKey);

            if (energy.get(player) < energyCapacity) {

                if (!player.isFlying() || inFlightGameMode(player)) {
                    // Regenerate energy if not flying or in creative/spectator mode
                    try {
                        // If the player is wearing elytra, regenerate 4x
                        // If also gliding, only 2x
                        int addition;
                        if (isPlayerWearingElytra(player)) {
                            if (player.isGliding()) addition = 2;
                            else addition = 4;
                        }
                        else {
                            addition = 1;
                        }

                        energy.compute(player, (k, i) -> i + addition);
                    } catch (NullPointerException e) {
                        energy.put(player, energyCapacity);
                    }
                }

                if (energy.get(player) > energyCapacity) energy.put(player, energyCapacity);

                bossBar = configureBossbar(player, bossBar, namespacedKey);

                // Remove player from flight if energy runs out
                if (energy.get(player) <= 0) {

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

                    // Armor points penalty
                    final double armorPenalty;
                    if (isPlayerEquipmentHeavy(player)) armorPenalty = 1;
                    else armorPenalty = 0;

                    // Calculate the total energy to remove
                    final double energyToRemove = movement2d * flightEnergyDepletionXYMultiplier + movementY * flightEnergyDepletionYMultiplier + armorPenalty;

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
            } else if (player.isGliding() && isPlayerWearingElytra(player)) {

                if (input.isJump() && riseCooldown.get(player) == 0 && !isFlightRestricted(player) && !isPlayerDepleted(player)) {
                    final Vector velocity = player.getVelocity();

                    // Add a bit of velocity based on facing direction
                    final Vector addedVelocity = player.getLocation().getDirection().normalize();
                    addedVelocity.multiply(0.15);
                    final Vector newVelocity = velocity.clone().add(addedVelocity);

                    // Set new velocity
                    player.setVelocity(newVelocity);

                    riseCooldown.put(player, 15);
                    playWingFlapSound(player);
                    energy.put(player, energy.get(player) - 50);
                    if (energy.get(player) < 0) energy.put(player, (double) 0);
                }
            }

            riseCooldown.putIfAbsent(player, 0);
            if (riseCooldown.get(player) > 0) riseCooldown.put(player, riseCooldown.get(player) - 1);
            if (!player.isGliding()) riseCooldown.put(player, 15);
        }
    }

    @NotNull
    private static KeyedBossBar configureBossbar(Player player, KeyedBossBar bossBar, NamespacedKey namespacedKey) {
        if (bossBar == null) { // Create bossbar if it's null

            // Yellow if player is not wearing elytra, green if they are
            BarColor color = BarColor.YELLOW;
            if (isPlayerWearingElytra(player)) color = BarColor.GREEN;

            bossBar = Bukkit.createBossBar(namespacedKey, "Energy", color, BarStyle.SEGMENTED_10);
            bossBar.addPlayer(player);
        }

        bossBar.setVisible(!inFlightGameMode(player));

        BarColor color = BarColor.YELLOW;
        // Set bossbar to red if it's depleted
        if (isPlayerDepleted(player)) color = BarColor.RED;
        else {
            if (!isPlayerEquipmentHeavy(player) && isPlayerWearingElytra(player)) color = BarColor.GREEN;
        }
        bossBar.setColor(color);

        // Set progress
        bossBar.setProgress(energy.get(player) / energyCapacity);
        return bossBar;
    }

    public static boolean isPlayerEquipmentHeavy(@NotNull Player player) {
        return Objects.requireNonNull(player.getAttribute(Attribute.ARMOR)).getValue() >= 12;
    }

    public static boolean isPlayerWearingElytra(@NotNull Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        return (chestplate != null && chestplate.getType().equals(Material.ELYTRA));
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
        Location centerPoint = player.getLocation();
        World world = player.getWorld();

        if (velocity.getY() >= -0.08) return false;

        Location point1 = centerPoint.clone().add((hitboxWidth / 2), 0, (hitboxWidth / 2));
        Location point2 = centerPoint.clone().add(-(hitboxWidth / 2), 0, (hitboxWidth / 2));
        Location point3 = centerPoint.clone().add((hitboxWidth / 2), 0, -(hitboxWidth / 2));
        Location point4 = centerPoint.clone().add(-(hitboxWidth / 2), 0, -(hitboxWidth / 2));

        RayTraceResult traceResult1 = world.rayTraceBlocks(point1, velocity, 1);
        RayTraceResult traceResult2 = world.rayTraceBlocks(point2, velocity, 1);
        RayTraceResult traceResult3 = world.rayTraceBlocks(point3, velocity, 1);
        RayTraceResult traceResult4 = world.rayTraceBlocks(point4, velocity, 1);
        RayTraceResult traceResult5 = world.rayTraceBlocks(centerPoint, velocity, 1);

        boolean overlapsPoint1 = (traceResult1 != null);
        boolean overlapsPoint2 = (traceResult2 != null);
        boolean overlapsPoint3 = (traceResult3 != null);
        boolean overlapsPoint4 = (traceResult4 != null);
        boolean overlapsPoint5 = (traceResult5 != null);

        return (overlapsPoint1 || overlapsPoint2 || overlapsPoint3 || overlapsPoint4 || overlapsPoint5);
    }

    public static void playWingFlapSound(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 1.2f);
    }
}
