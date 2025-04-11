package io.github.stonley890.dreamvisitor.data;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.earth2me.essentials.Essentials;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shanerx.mojang.Mojang;

public class PlayerUtility {
    private static final Map<String, PlayerMemory> MEMORY_MAP = new HashMap<>();

    private PlayerUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the specified player's memory from the file.
     * Creates a new configuration with default values if one does not exist.
     * @param uuid The UUID of the player whose data to fetch.
     * @return The {@link PlayerMemory} of the given player.
     */
    private static @NotNull PlayerMemory fetchPlayerMemory(@NotNull UUID uuid) {
        File file = new File(Dreamvisitor.getPlayerPath(uuid));
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        return PlayerMemory.getFromFileConfig(fileConfig);
    }

    /**
     * Saves the specified player's memory to file. Does nothing if the player is not in memory.
     * @param uuid The UUID of the player whose data to save.
     */
    public static void savePlayerMemory(@NotNull UUID uuid) throws IOException {
        if(MEMORY_MAP.containsKey(uuid.toString())) {
            PlayerMemory memory = getPlayerMemory(uuid);
            memory.toFileConfig().save(Dreamvisitor.getPlayerPath(uuid));
        }
    }

    /**
     * Get the specified player's memory. If it is not in memory, it will be fetched from file.
     * @param uuid The UUID of the player whose data to get.
     * @return The {@link PlayerMemory} of the given player.
     */
    public static @NotNull PlayerMemory getPlayerMemory(@NotNull UUID uuid) {
        // If it does not exist in memory, add it
        if(!MEMORY_MAP.containsKey(uuid.toString())) {
            PlayerMemory memory = fetchPlayerMemory(uuid);
            MEMORY_MAP.put(uuid.toString(), memory);
            return memory;
        }
        return MEMORY_MAP.get(uuid.toString());
    }

    /**
     * Removes the specified player's memory from random access storage. This does NOT save memory first.
     * @param uuid The UUID of the player whose data to remove.
     */
    public static void clearPlayerMemory(@NotNull UUID uuid) {
        MEMORY_MAP.remove(uuid.toString());
    }

    /**
     * Update a player's memory configuration. This must be used to update a player's memory after it has been modified.
     * @param uuid The UUID of the player whose data to modify.
     * @param memory The modified {@link PlayerMemory}.
     */
    public static void setPlayerMemory(@NotNull UUID uuid, @NotNull PlayerMemory memory) {
        MEMORY_MAP.put(uuid.toString(), memory);
    }

    /**
     * Adds the hyphens back into a String UUID.
     * @param uuid the UUID as a {@link String} without hyphens.
     * @return a UUID as a string with hyphens.
     */
    @Contract(pure = true)
    public static @NotNull String formatUuid(@NotNull String uuid) throws NullPointerException {

        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
    }

    public static @Nullable String getUsernameOfUuid(@NotNull UUID uuid) {
        Mojang mojang = new Mojang();
        return mojang.getPlayerProfile(uuid.toString()).getUsername();
    }

    public static @Nullable String getUsernameOfUuid(@NotNull String uuid) {
        Mojang mojang = new Mojang();
        return mojang.getPlayerProfile(uuid).getUsername();
    }


    public static @Nullable UUID getUUIDOfUsername(@NotNull String username) {
        Mojang mojang = new Mojang();
        String uuid = mojang.getUUIDOfUsername(username);
        if (uuid == null) return null;
        try {
            return UUID.fromString(formatUuid(uuid));
        } catch (IllegalArgumentException NullPointerException) {
            return null;
        }
    }

    /**
     * Get the {@link Instant} of player's last login by UUID.
     * @param uuid the UUID of the player.
     * @return the {@link Instant} this player last logged in.
     * @throws Exception if Essentials is not enabled.
     */
    public static @NotNull Instant getLastLogin(@NotNull UUID uuid) throws Exception {
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) {
            throw new Exception("EssentialsX is not enabled.");
        }
        return Instant.ofEpochMilli(ess.getUser(uuid).getLastLogin());
    }

    /**
     * Get the {@link Instant} of player's last logout by UUID.
     * @param uuid the UUID of the player.
     * @return the {@link Instant} this player last logged out.
     * @throws Exception if Essentials is not enabled.
     */
    public static @NotNull Instant getLastLogout(@NotNull UUID uuid) throws Exception {
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) {
            throw new Exception("EssentialsX is not enabled.");
        }
        return Instant.ofEpochMilli(ess.getUser(uuid).getLastLogout());
    }
}
