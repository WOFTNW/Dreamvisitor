package io.github.stonley890.dreamvisitor.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shanerx.mojang.Mojang;

import javax.net.ssl.HttpsURLConnection;

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

    public static boolean setSkin(Player player, UUID uuid) {
        GameProfile profile = ((CraftPlayer) player).getHandle().gh();
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuid)).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                StringBuilder replyBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while (true) {
                    try {
                        String str = bufferedReader.readLine();
                        if (str == null) break;
                        replyBuilder.append(str);
                    } catch (IOException e) {
                        break;
                    }
                }
                String reply = replyBuilder.toString();
                Dreamvisitor.debug("REPLY: " + reply);
//                String skin = reply.split("\"value\" : \"")[1].split("\"")[0];
//                String signature = reply.split("\"signature\" : \"")[1].split("\"")[0];
                PropertyMap properties = profile.getProperties();
                properties.remove("textures", properties.get("textures").iterator().next());
                properties.put("textures", new Property(
                        "textures",
                        "ewogICJ0aW1lc3RhbXAiIDogMTc0MzAzMDM1NDU5NCwKICAicHJvZmlsZUlkIiA6ICJmZjQ3NzI5YmQwZDI0YWYwOThiMTFjMGE3ZTFiMGVlZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXRzY2FuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZjNzFhZGEzYWFiZDljNGM4ZGNiYTk3NDE1M2MzYWRkMWZiYzRkZTU5N2YyNzY1NDdmODBlMzEzMzM3MTcwOGIiCiAgICB9CiAgfQp9",
                        "pDIF7DI/4q1yLHMftWJTYlmRqCET+k1LImaYbhMmoFXRFiE56wszKGr5ZQ8lNeOs9/IOD41l+vHkSKKT36djX8kPVjl0k9AFx5kWa0wU9tmk7PNZAk94HJhOwtaGF2x/1oWTZ+saTE+Tb03DQ7/xvtIBfuLYVSMaCi1Ugd6uwD8YM9weLrad9VMn/bMGdP/Xx2tVdh8PQW180abyDkzVv3cw2agNo58+nAVYCXvRmQgVEGd/bxK8XR/iBz5ocvD47VblPUNjEHbtQHtlxzPbbjhDZ2h/kqQdNy0kmug73s0gW5UJIPNJRoIo8IrYh/sldFyBydSynQVBtE2PMA7RAb/hi7c/0gf5GDsljrhORzLvjMvy9LEGvD9PsGjfocMdHKLg+mwizJmHChPwBCdSOYykaY/LILOGMBXQr6e6wKzW+ajy4928NTpbsM4Fvwu82zAWIZjwm0IFR1ZWLAuSTUTIW1gLXRUFXwWORajAu+Kivz1n0wiV8UkK/NEVYUc4fqxBrERafia2ERUDPhYNLgIjATcXL+/JSVvkSCdtuOzyxlzhNfKdse7GN7k8iOQVcouPSDWq3sTKs6plMslsrm8RxUK2P2mhxuxHVeKvgvzVT1yCjDc6zKzYVY1zpoVlOm2ychGjC+d/CrVz3MkZa3oKB5V3B2TwBqcTLIg3NhE="
                ));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hidePlayer(Dreamvisitor.getPlugin(), player);
                    p.showPlayer(Dreamvisitor.getPlugin(), player);
                }

                return true;
            } else {
                System.out.println("Connection could not be opened (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
