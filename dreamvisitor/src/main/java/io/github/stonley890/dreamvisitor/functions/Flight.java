package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Input;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
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
    private static final Map<Player, Boolean> falling = new HashMap<>();
    private static final Map<Player, Vector> lastPosition = new HashMap<>();
    private static final Map<Player, Float> fallDistance =new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

                // If player does not have the dreamvisitor.fly permission, disable flight if not in creative
                if ((!player.hasPermission("dreamvisitor.fly") || isFlightRestricted(player) && !inFlightGameMode(player)) || (memory.flightDisabled && !inFlightGameMode(player))) {
                    player.setAllowFlight(false);
                } else setupFlight(player);

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
            }
        }, 0, 0);
    }

    public static boolean isPlayerDepleted(Player player) {
        return (energyDepletion.computeIfAbsent(player, k -> false));
    }

    public static void setPlayerDepleted(Player player, boolean depleted) {
        energyDepletion.put(player, depleted);
    }

    /**
     * Whether flight is restricted by a WorldGuard region.
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
            }
            else setupFlight(player);

        }

    }

    public static void setupFlight(@NotNull Player player) {
        // Re-enable flight if it gets disabled by game mode change
        // Dreamvisitor.debug("FlightNotAllowed: " + !player.getAllowFlight() + " Permission: " + player.hasPermission("dreamvisitor.fly") + " NotRestricted: " + !isFlightRestricted(player) + " NotDepleted: " + !isPlayerDepleted(player) + " NotDisabled: " + !PlayerUtility.getPlayerMemory(player.getUniqueId()).flightDisabled);
        if (!player.getAllowFlight() && player.hasPermission("dreamvisitor.fly") && !isFlightRestricted(player) && !isPlayerDepleted(player) && !PlayerUtility.getPlayerMemory(player.getUniqueId()).flightDisabled) {
            Dreamvisitor.debug("All requirements met for flight.");
            Bukkit.getScheduler().runTaskLater(Dreamvisitor.getPlugin(), () -> player.setAllowFlight(true), 1);
        }
    }

    /**
     * Whether a player is in a flight-enabled game mode like Creative or Spectator.
     * @param player
     * @return
     */
    public static boolean inFlightGameMode(@NotNull Player player) {
        return (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
    }

    public static void tick() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
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
                    if (lastLoc != null && Objects.equals(currentLoc.getX(), lastLoc.getX()) && Objects.equals(currentLoc.getZ(), lastLoc.getZ())) movement2d = 0;

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
            } else if (!isFlightRestricted(player) && !isPlayerDepleted(player)) {

                // This is a reimplementation of fall damage because enabling flight disables fall damage.

                // If the player is not flying (but is able to), check falling
                if (player.getFallDistance() > 0 && !falling.get(player) && !player.isFlying()) {
                    // If the player is falling, record that
                    falling.put(player, true);
                } else if (player.getFallDistance() == 0) {
                    // If not, check if they hit ground or are flying
                    if (player.isFlying()) {
                        // If flying, mark not falling
                        falling.put(player, false);
                    } else {
                        // FALL DAMAGER TIME
                        int damage = calculateFallDamage(
                                fallDistance.get(player),
                                Objects.requireNonNull(player.getAttribute(Attribute.SAFE_FALL_DISTANCE)).getValue(),
                                Objects.requireNonNull(player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER)).getValue()
                        );
                        player.damage(damage, DamageSource.builder(DamageType.FALL).build());
                        falling.put(player, false);
                    }
                }
                fallDistance.put(player, player.getFallDistance());
            }
        }

    }

    /**
     * Calculates fall damage using the formula:
     * fallDamage = Max(0, Ceiling[(fallDistance − safeFallDistance) × fallDamageMultiplier])
     *
     * @param fallDistance The total distance the entity has fallen (in meters or game units).
     * @param safeFallDistance The distance that can be fallen without taking any damage.
     * @param fallDamageMultiplier The multiplier applied to the damage taken per unit over the safe distance.
     * @return The total fall damage (rounded up, never less than 0).
     */
    public static int calculateFallDamage(double fallDistance, double safeFallDistance, double fallDamageMultiplier) {
        double excessFall = fallDistance - safeFallDistance;
        double rawDamage = excessFall * fallDamageMultiplier;

        // Ensure damage is not negative and round up to nearest whole number
        return (int) Math.max(0, Math.ceil(rawDamage));
    }
}
