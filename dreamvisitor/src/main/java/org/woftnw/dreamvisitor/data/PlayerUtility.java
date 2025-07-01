package org.woftnw.dreamvisitor.data;

import java.time.Instant;
import java.util.*;

import com.earth2me.essentials.Essentials;
import org.bukkit.entity.Player;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shanerx.mojang.Mojang;
import org.woftnw.dreamvisitor.data.repository.UserRepository;
import org.woftnw.dreamvisitor.data.type.DVUser;

public class PlayerUtility {
    private static final List<DVUser> USER_MEMORY = new ArrayList<>();

    private static final UserRepository userRepository = Dreamvisitor.getPlugin().getRepositoryManager().getUserRepository();

    private PlayerUtility() {
        throw new IllegalStateException("Utility class");
    }

    @NotNull
    public static DVUser getUser(@NotNull UUID uuid) {
        // Check if in memory map
        for (DVUser user : USER_MEMORY) {
            if (Objects.equals(user.getMinecraftUuid(), uuid)) {
                return user;
            }
        }

        // Get from repository
        Optional<DVUser> optional = userRepository.findByUuid(uuid);
        if (optional.isPresent()) {
            // If it exists, add to memory map and return
            USER_MEMORY.add(optional.get());
            return optional.get();
        }

        // If not, create, add to memory map, and return
        DVUser user = new DVUser();
        user.setMinecraftUuid(uuid);
        USER_MEMORY.add(user);
        return user;
    }

    @NotNull
    public static DVUser getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public static void saveUser(DVUser user) {

        DVUser existing = null;
        for (DVUser dvUser : USER_MEMORY) {
            if (Objects.equals(dvUser.getMinecraftUuid(), user.getMinecraftUuid())) {
                existing = dvUser;
                break;
            }
        }
        if (existing != null) USER_MEMORY.remove(existing);

        USER_MEMORY.add(user);

        userRepository.save(existing);
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
