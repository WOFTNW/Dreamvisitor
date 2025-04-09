package io.github.stonley890.dreamvisitor.functions;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

public class Moonglobe {

    public static final List<Moonglobe> activeMoonglobes = new ArrayList<>();
    private final static float momentumMultiplier = 0.1f;

    private final UUID player;
    private final Location origin;
    private final float allowedDistance;
    private Location currentLocation;
    private boolean shown = false;
    private ItemDisplay glowEntity = null;
    private boolean remove = false;


    public Moonglobe(@NotNull UUID owner, @NotNull Location originLocation, float allowedOriginDistance) {

        player = owner;
        origin = originLocation;
        currentLocation = originLocation;
        allowedDistance = allowedOriginDistance;

        activeMoonglobes.add(this);

    }

    public static void tick() {

        if (activeMoonglobes.isEmpty()) return;

        Iterator<Moonglobe> iterator = activeMoonglobes.iterator();

        while (iterator.hasNext()) {
            Moonglobe activeMoonglobe = iterator.next();

            Player onlinePlayer = Bukkit.getPlayer(activeMoonglobe.player);
            if (onlinePlayer == null) {
                if (activeMoonglobe.shown) activeMoonglobe.hideGlobe();
            } else {

                if (activeMoonglobe.remove) {
                    iterator.remove();
                    activeMoonglobes.remove(activeMoonglobe);
                    return;
                }

                if ((!Objects.equals(activeMoonglobe.origin.getWorld(), onlinePlayer.getLocation().getWorld())) || (activeMoonglobe.origin.distance(activeMoonglobe.currentLocation) > activeMoonglobe.allowedDistance)) {
                    activeMoonglobe.remove("Too far away from origin.");
                    return;
                }

                if (!activeMoonglobe.shown) activeMoonglobe.showGlobe();

                Location targetPosition = getTargetPosition(onlinePlayer);

                Vector posDifference = targetPosition.subtract(activeMoonglobe.currentLocation).toVector();
                Vector momentum = posDifference.multiply(momentumMultiplier);

                Location newLocation = activeMoonglobe.currentLocation.clone().add(momentum);

                Block oldBlock = activeMoonglobe.currentLocation.getBlock();
                Block newBlock = newLocation.getBlock();

                boolean sameBlock = Objects.deepEquals(newBlock.getLocation(), oldBlock.getLocation());

                if (!sameBlock) {
                    if (newBlock.getType().equals(Material.AIR)) newBlock.setType(Material.LIGHT);
                    if (oldBlock.getType().equals(Material.LIGHT)) oldBlock.setType(Material.AIR);
                }

                activeMoonglobe.currentLocation = newLocation;
                activeMoonglobe.glowEntity.teleport(activeMoonglobe.currentLocation);
            }

        }
    }

    private static @NotNull Location getTargetPosition(@NotNull Player onlinePlayer) {
        Location eyeLocation = onlinePlayer.getEyeLocation();

        // x is multiplied by -1 because (x, y) on unit circle represents (z, -x) top-down in-game.
        float radius = 0.75f; // distance from player
        float rotation = eyeLocation.getYaw() - 90;
        return eyeLocation.add(
                radius * Math.sin(Math.toRadians(rotation)),
                0,
                radius * -1 * Math.cos(Math.toRadians(rotation))
        );
    }

    public void remove(@Nullable String reason) {
        remove = true;
        hideGlobe();
        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null && reason != null) onlinePlayer.sendMessage(ChatColor.RED + "Your moon globe was removed: " + reason);

    }

    private void showGlobe() {

        glowEntity = (ItemDisplay) Objects.requireNonNull(currentLocation.getWorld()).spawnEntity(currentLocation, EntityType.ITEM_DISPLAY);
        glowEntity.setItemStack(new ItemStack(Material.SEA_LANTERN));
        glowEntity.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(0.5f, 0.5f, 0.5f), new Quaternionf()));
        shown = true;

    }

    private void hideGlobe() {

        if (currentLocation.getBlock().getType().equals(Material.LIGHT)) currentLocation.getBlock().setType(Material.AIR);
        glowEntity.remove();
        shown = false;
    }

    public UUID getPlayer() { return player; }

}
