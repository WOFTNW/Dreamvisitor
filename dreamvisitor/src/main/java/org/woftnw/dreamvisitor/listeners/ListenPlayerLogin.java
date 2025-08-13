package org.woftnw.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.woftnw.dreamvisitor.data.Config;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.util.ConfigKey;

public class ListenPlayerLogin implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @EventHandler
    public void onPlayerLoginEvent(@NotNull PlayerLoginEvent event) {

        Player player = event.getPlayer();

        if (player.hasPermission("dreamvisitor.nowhitelist")) {

            // Always allow ops
            event.allow();

        } else if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {

            // Always deny banned
            event.disallow(Result.KICK_BANNED, "You are banned.");

        } else if (event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST) {

            // Always deny non-whitelisted
            event.disallow(Result.KICK_WHITELIST, "You are not whitelisted.");

        } else {
            // Player limit is not overridden

            // If soft whitelist is on
            if (Config.get(ConfigKey.SOFT_WHITELIST)) {

                // Soft whitelist is enabled
                Dreamvisitor.getPlugin().getLogger().info("Soft whitelist is enabled");
                allowIfSoftWhitelist(player, event);
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    void allowIfSoftWhitelist(Player player, PlayerLoginEvent event) {

        // Initialize file
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        // Load file
        try {
            fileConfig.load(file);
        } catch (IOException | InvalidConfigurationException ignored) {
            event.disallow(Result.KICK_OTHER, "You are not allowed at this time.");
            return;
        }

        // Fetch soft-whitelisted players
        List<String> whitelistedPlayers = (List<String>) fileConfig.get("players");

        // If a player is on soft whitelist, allow. If not, kick player.
        assert whitelistedPlayers != null;
        if ((whitelistedPlayers.contains(player.getUniqueId().toString()))) {
            event.allow();

        } else {
            event.disallow(Result.KICK_OTHER, "You are not allowed at this time.");
        }
    }

}
