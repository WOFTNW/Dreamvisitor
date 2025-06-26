package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Messager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class PlayerTribe {

    private static final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    private static final File file = new File(plugin.getDataFolder(), "player-tribes.yml");

    /**
     * Initializes the player tribe storage.
     *
     * @throws IOException If the file could not be created.
     */
    public static void setup() throws IOException {

        if (!file.exists()) {
            Bukkit.getLogger().info("player-tribes.yml does not exist. Creating one...");
            file.createNewFile();
        }
    }

    @Nullable
    private static Tribe getPlayer(@NotNull UUID uuid) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(file);
            return Tribe.valueOf(configuration.getString(uuid.toString()));
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Unable to load " + file.getName() + "!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException();
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Saves the current file configuration to disk.
     */
    private static void savePlayer(@NotNull UUID uuid, @NotNull Tribe tribe) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(file);
            configuration.set(uuid.toString(), tribe.toString());
            configuration.save(file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Unable to load " + file.getName() + "!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException();
        }
    }

    /**
     * Gets the stored tribe of a given player. If it does not exist, this method will try to update it.
     *
     * @param playerUuid The player UUID to search for.
     * @return The index of their tribe.
     */
    @Nullable
    public static Tribe getTribeOfPlayer(@NotNull UUID playerUuid) {

        Tribe tribe = getPlayer(playerUuid);

        // If not in file, try to update
        if (tribe == null) {
            try {
                updateTribeOfPlayer(playerUuid);
                tribe = getPlayer(playerUuid);
            } catch (Exception e) {
                return tribe;
            }
        }

        return tribe;

    }

    /**
     * Attempts to update a player's recorded tribe by team.
     * If not found on a team, tags will be checked if player is online.
     * @param uuid the UUID of the player to update, online or offline.
     * @throws NullPointerException given player does not have a valid team or tag associated with a tribe.
     */
    public static void updateTribeOfPlayer(@NotNull UUID uuid) throws NullPointerException {

        boolean online;
        Player player = Bukkit.getPlayer(uuid);

        online = player != null && player.isOnline();

        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

        int playerTribe;

        String username;
        if (online) username = player.getName();
        else username = PlayerUtility.getUsernameOfUuid(uuid);

        if (username == null) throw new NullPointerException("Player is null");

        // Check by team
        Messager.debug("Checking by team...");
        Tribe[] tribes = Tribe.values();
        for (int i = 0; i < tribes.length; i++) {
            Tribe tribe = tribes[i];
            Team team = scoreboard.getTeam(tribe.getTeamName());
            Messager.debug("Checking team " + i);
            if (team != null && team.hasEntry(username)) {
                Messager.debug("Found team " + i);
                playerTribe = i;
                savePlayer(uuid, tribes[playerTribe]);
                return;
            }
        }
        Messager.debug("Could not find by team.");

        if (online) {
            // If no matching team, check by tags
            Messager.debug("Checking by tag...");
            for (int i = 0; i < tribes.length; i++) {
                Tribe tribe = tribes[i];
                Messager.debug("Checking tag " + i);
                if (player.getScoreboardTags().contains(tribe.getTeamName())) {
                    Messager.debug("Found tag " + i);
                    playerTribe = i;
                    savePlayer(uuid, tribes[playerTribe]);
                    return;
                }
            }
        }

        throw new NullPointerException("Given player does not have a valid team or tag associated with a tribe!");
    }

    /**
     * This will use the saved royalty board to fill information.
     * @param uuid the UUID of player
     */
    public static void updatePermissions(@NotNull UUID uuid) {
        if (Dreamvisitor.luckperms != null) {

            Messager.debug("[updatePermissions] Updating permissions for user " + uuid + " using info finding");

            Tribe tribe;

            tribe = PlayerTribe.getTribeOfPlayer(uuid);

            // Run async
            if (Dreamvisitor.luckperms != null) {

                Messager.debug("[updatePermissions] Updating permissions for user " + uuid);

                // Get user manager
                UserManager userManager = Dreamvisitor.luckperms.getUserManager();
                // Get user at tribe t and position p

                Messager.debug("[updatePermissions] Updating permissions of UUID " + uuid + " of tribe " + tribe);

                // Run async
                userManager.modifyUser(uuid, user -> {

                    // For each tribe and position...
                    for (String tribeName : Arrays.stream(TribeUtil.tribes).map(tribeCheck -> tribeCheck.getTeamName().toLowerCase()).toList()) {

                        // remove it from player

                        if (tribeName != null) {
                            Messager.debug("[updatePermissions] Removing group " + tribeName + " from " + uuid);
                            // Get the group from lp and remove it from the user.
                            user.data().remove(Node.builder("group." + tribeName).build());

                        }

                    }

                    // Now that all have been removed, add the correct one
                    if (tribe != null) {

                        String groupName = tribe.getTeamName().toLowerCase();

                        Messager.debug("[updatePermissions] Adding group " + groupName + " to " + uuid);
                        // Get the group from lp and add it to the user.
                        user.data().add(Node.builder("group." + groupName).build());
                    }

                });
            } else
                Bukkit.getLogger().warning("Dreamvisitor could not hook into LuckPerms on startup. Permission update failed.");
        } else
            Bukkit.getLogger().warning("Dreamvisitor could not hook into LuckPerms on startup. Permission update failed.");
    }


}
