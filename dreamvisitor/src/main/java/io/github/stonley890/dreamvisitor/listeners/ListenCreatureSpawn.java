package io.github.stonley890.dreamvisitor.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ListenCreatureSpawn implements Listener {

    @EventHandler
    public void onCreatureSpawn(@NotNull CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            org.bukkit.Location bukkitLocation = event.getLocation();
            Location location = BukkitAdapter.adapt(bukkitLocation);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            boolean noWither = !query.testState(location, null, Dreamvisitor.WITHER);

            if (noWither) {
                event.setCancelled(true);
                World world = bukkitLocation.getWorld();
                assert world != null;
                Collection<Entity> nearbyEntities = world.getNearbyEntities(bukkitLocation, 10, 10, 10);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player player) {
                        player.sendMessage(ChatColor.RED + Dreamvisitor.getPlugin().getConfig().getString("noWitherNotice"));
                    }
                }
            }
        }
    }

}
