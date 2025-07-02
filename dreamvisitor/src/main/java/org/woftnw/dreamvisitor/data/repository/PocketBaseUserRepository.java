package org.woftnw.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.pb.PocketBase;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PocketBase implementation of the UserRepository interface
 */
public class PocketBaseUserRepository implements UserRepository {
    private static final Logger LOGGER = Logger.getLogger(PocketBaseUserRepository.class.getName());
    private static final String COLLECTION_NAME = "users";
    private final PocketBase pocketBase;
    private final Gson gson;

    /**
     * Constructor for PocketBaseUserRepository
     *
     * @param pocketBase The PocketBase client to use
     */
    public PocketBaseUserRepository(PocketBase pocketBase) {
        this.pocketBase = pocketBase;
        this.gson = new Gson();
    }

    @Override
    public Optional<DVUser> findById(String id) {
        try {
            JsonObject record = pocketBase.getRecord(COLLECTION_NAME, id, null, null);
            return Optional.of(mapToUser(record));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error finding user by ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<DVUser> findByUuid(@NotNull UUID mc_uuid) {
        try {
            String filter = "mc_uuid = '" + mc_uuid.toString() + "'";
            JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
            return Optional.of(mapToUser(record));
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "No user found with UUID: " + mc_uuid);
            return Optional.empty();
        }
    }

    @Override
    public Optional<DVUser> findByDiscordId(long discordId) {
        try {
            String filter = "discord_id = '" + discordId + "'";
            JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
            return Optional.of(mapToUser(record));
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "No user found with Discord ID: " + discordId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<DVUser> findByMcUsername(String mcUsername) {
        try {
            String filter = "mc_username = '" + mcUsername + "'";
            JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
            return Optional.of(mapToUser(record));
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "No user found with MC username: " + mcUsername);
            return Optional.empty();
        }
    }

    @Override
    public List<DVUser> findAll() {
        try {
            List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, null, null, null);
            return records.stream()
                    .map(this::mapToUser)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error retrieving all users", e);
            return Collections.emptyList();
        }
    }

    @Override
    public DVUser save(DVUser user) {
        try {
            JsonObject userData = mapToJsonObject(user);

            if (user.getId() != null && !user.getId().isEmpty()) {
                // Update existing user
                JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, user.getId(), userData, null, null);
                return mapToUser(updatedRecord);
            } else {
                // Create new user
                JsonObject newRecord = pocketBase.createRecord(COLLECTION_NAME, userData, null, null);
                return mapToUser(newRecord);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving user: " + user.getMinecraftUsername(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public void delete(@NotNull DVUser user) {
        if (user.getId() != null) {
            deleteById(user.getId());
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            pocketBase.deleteRecord(COLLECTION_NAME, id);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user with ID: " + id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Convert a JsonObject from PocketBase to a User object
     *
     * @param json JsonObject from PocketBase API
     * @return Mapped User object
     */
    @NotNull
    private DVUser mapToUser(JsonObject json) {
        DVUser user = new DVUser();

        user.setId(getStringOrNull(json, "id"));
        user.setCollectionId(getStringOrNull(json, "collectionId"));
        user.setCollectionName(getStringOrNull(json, "collectionName"));

        user.setDiscordId(getLongOrNull(json, "discord_id"));
        user.setDiscordUsername(getStringOrNull(json, "discord_username"));
        user.setDiscordAvatarUrl(getStringOrNull(json, "discord_img"));
        user.setMinecraftUsername(getStringOrNull(json, "mc_username"));

        if (json.has("mc_uuid") && !json.get("mc_uuid").isJsonNull()) {
            try {
                user.setMinecraftUuid(UUID.fromString(PlayerUtility.formatUuid(json.get("mc_uuid").getAsString())));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid UUID format: " + json.get("mc_uuid").getAsString());
            }
        }

        // Parse relation lists
        user.setInfractions(getStringListOrEmpty(json, "infractions"));
        user.setHomes(getStringListOrEmpty(json, "users_home"));
        user.setInventory_items(getStringListOrEmpty(json, "inventory_items"));
        user.setClaims(getStringListOrEmpty(json, "claims"));

        // Parse numeric fields
        user.setClaimLimit(getIntOrNull(json, "claim_limit"));
        user.setPlayTime(getIntOrNull(json, "play_time"));
        user.setBalance(getDoubleOrNull(json, "balance"));
        user.setDaily_streak(getIntOrNull(json, "daily_streak"));

        // Parse boolean fields
        user.setIsSuspended(getBooleanOrNull(json, "is_suspended"));
        user.setIsBanned(getBooleanOrNull(json, "is_banned"));
        user.setShowDiscordMessages(getBooleanOrNull(json, "show_discord_messages"));
        user.setFlightDisabled(getBooleanOrNull(json, "flight_disabled"));
        user.setVanished(getBooleanOrNull(json, "vanished"));
        user.setAutoInvSwapEnabled(getBooleanOrNull(json, "auto_inv_swap_enabled"));
        user.setAutoRadioEnabled(getBooleanOrNull(json, "auto_radio_enabled"));
        user.setInSandboxMode(getBooleanOrNull(json, "in_sandbox_mode"));

        // Parse datetime fields
        user.setLastWork(getOffsetDateTimeOrNull(json, "last_work"));
        user.setLast_Played(getOffsetDateTimeOrNull(json, "last_played"));
        user.setLastDaily(getOffsetDateTimeOrNull(json, "last_daily"));
        user.setCreated(getOffsetDateTimeOrNull(json, "created"));
        user.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

        return user;
    }

    /**
     * Convert a User object to a JsonObject for PocketBase
     *
     * @param user User object to convert
     * @return JsonObject for PocketBase API
     */
    @NotNull
    private JsonObject mapToJsonObject(@NotNull DVUser user) {
        JsonObject json = new JsonObject();

        // Only include fields that PocketBase expects for updates/creates
        if (user.getDiscordId() != null)
            json.addProperty("discord_id", user.getDiscordId());
        if (user.getDiscordUsername() != null)
            json.addProperty("discord_username", user.getDiscordUsername());
        if (user.getDiscordAvatarUrl() != null)
            json.addProperty("discord_img", user.getDiscordAvatarUrl());
        if (user.getMinecraftUsername() != null)
            json.addProperty("mc_username", user.getMinecraftUsername());
        if (user.getMinecraftUuid() != null)
            json.addProperty("mc_uuid", user.getMinecraftUuid().toString());

        // Add numeric fields
        if (user.getClaimLimit() != null)
            json.addProperty("claim_limit", user.getClaimLimit());
        if (user.getPlayTime() != null)
            json.addProperty("play_time", user.getPlayTime());
        if (user.getBalance() != null)
            json.addProperty("balance", user.getBalance());
        if (user.getDaily_streak() != null)
            json.addProperty("daily_streak", user.getDaily_streak());

        // Add boolean fields
        if (user.getIsSuspended() != null)
            json.addProperty("is_suspended", user.getIsSuspended());
        if (user.getIsBanned() != null)
            json.addProperty("is_banned", user.getIsBanned());
        if (user.isShowDiscordOn() != null)
            json.addProperty("show_discord_messages", user.isShowDiscordOn());
        if (user.isFlightDisabled() != null)
            json.addProperty("flight_disabled", user.isFlightDisabled());
        if (user.isVanished() != null)
            json.addProperty("vanished", user.isVanished());
        if (user.isAutoInvSwapEnabled() != null)
            json.addProperty("auto_inv_swap_enabled", user.isAutoInvSwapEnabled());
        if (user.isAutoRadioEnabled() != null)
            json.addProperty("auto_radio_enabled", user.isAutoRadioEnabled());
        if (user.isInSandboxMode() != null)
            json.addProperty("in_sandbox_mode", user.isInSandboxMode());
        if (user.isUsingCreativeInv() != null)
            json.addProperty("using_creative_inv", user.isUsingCreativeInv());

        // Format and add datetime fields
        if (user.getLastWork() != null)
            json.addProperty("last_work", formatDateTime(user.getLastWork()));
        if (user.getLastDaily() != null)
            json.addProperty("last_daily", formatDateTime(user.getLastDaily()));

        // Add relation fields (these need to be handled separately based on
        // PocketBase's expectations)
        // For now, we'll just leave them out as they typically require special handling
        // TODO:Add relation fields
        return json;
    }

    @NotNull
    private List<String> getStringListOrEmpty(@NotNull JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull() && json.get(key).isJsonArray()) {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return gson.fromJson(json.get(key), listType);
        }
        return new ArrayList<>();
    }

    @Override
    public List<DVUser> getAllWhere(String filter) {
        try {
            List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, filter, null, null, null);
            return records.stream()
                    .map(this::mapToUser)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error retrieving users with filter: " + filter, e);
            return Collections.emptyList();
        }
    }
}
