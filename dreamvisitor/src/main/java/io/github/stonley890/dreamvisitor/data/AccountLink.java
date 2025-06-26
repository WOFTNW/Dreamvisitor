package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class AccountLink {

    static final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    static final File file = new File(plugin.getDataFolder().getPath() + "/accountLink.txt");

    @NotNull static final Map<UUID, Long> uuidToDiscordIdMap = new HashMap<>();
    @NotNull static final Map<Long, UUID> discordIdToUuidMap = new HashMap<>();

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Messager.debug("accountLink.txt does not exist. Creating one now...");
            try {
                if (!file.createNewFile()) throw new IOException("The existence of " + file.getName() + " cannot be verified!", null);
            } catch (IOException e) {
                throw new IOException("Dreamvisitor tried to create " + file.getName() + ", but it cannot be read/written! Does the server have read/write access?", e);
            }
        }
        loadFromFile();
    }

    private static void loadFromFile() {
        Messager.debug("Loading accountLink.txt");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    UUID uuid = UUID.fromString(PlayerUtility.formatUuid(parts[0]));
                    long discordID = Long.parseLong(parts[1]);
                    uuidToDiscordIdMap.put(uuid, discordID);
                    discordIdToUuidMap.put(discordID, uuid);
                }
            }
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().severe("accountLink.txt does not exist! " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        } catch (IOException e) {
            Bukkit.getLogger().severe("There was an error reading from accountLink.txt! " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
    }

    public static void saveFile() {
        Messager.debug("Saving...");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (Map.Entry<UUID, Long> entry : uuidToDiscordIdMap.entrySet()) {
                UUID uuid = entry.getKey();
                Messager.debug("UUID for this entry: " + uuid.toString());
                long discordId = entry.getValue();
                Messager.debug("Discord ID for this entry: " + discordId);

                String data = uuid.toString().replaceAll("-", "") + ":" + discordId;

                writer.write(data);
                writer.newLine();
                Messager.debug("Line written");
            }
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("There was an error writing to accountLink.txt! " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }

    }

    public static void linkAccounts(@NotNull UUID minecraftUUID, @NotNull Long discordId) {

        loadFromFile();

        if (Objects.equals(getUuid(discordId), minecraftUUID) && getDiscordId(minecraftUUID) == discordId) return;

        // remove existing values
        for (UUID uuid : uuidToDiscordIdMap.keySet()) {
            if (uuidToDiscordIdMap.get(uuid).equals(discordId)) {
                uuidToDiscordIdMap.remove(uuid);
            }
        }
        for (Long id : discordIdToUuidMap.keySet()) {
            if (discordIdToUuidMap.get(id).equals(minecraftUUID)) {
                discordIdToUuidMap.remove(discordId);
            }
        }

        // set values
        uuidToDiscordIdMap.put(minecraftUUID, discordId);
        discordIdToUuidMap.put(discordId, minecraftUUID);
        Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), AccountLink::saveFile);
    }

    /**
     * Get the Discord ID of the given {@link UUID}.
     * @param minecraftUUID the {@link UUID} to get the Discord ID of.
     * @return the {@code long} Discord ID.
     * @throws NullPointerException if the given {@link UUID} does not have an associated Discord ID.
     */
    public static long getDiscordId(@NotNull UUID minecraftUUID) {
        loadFromFile();
        return uuidToDiscordIdMap.get(minecraftUUID);
    }

    /**
     * Get the {@link UUID} of the given Discord ID.
     * @param discordId the {@code long} Discord ID to get the {@link UUID} of.
     * @return the {@link UUID} associated with this Discord ID or {@code null} if it does not exist.
     */
    public static @Nullable UUID getUuid(long discordId) {
        loadFromFile();
        return discordIdToUuidMap.get(discordId);
    }
}
