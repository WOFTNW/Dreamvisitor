package io.github.stonley890.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.stonley890.dreamvisitor.pb.PocketBase;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PocketBase implementation of the AltRepository interface
 */
public class PocketBaseAltRepository implements AltRepository {
  private static final Logger LOGGER = Logger.getLogger(PocketBaseAltRepository.class.getName());
  private static final String COLLECTION_NAME = "user_alts";
  private final PocketBase pocketBase;
  private final Gson gson;
  private final UserRepository userRepository;

  /**
   * Constructor for PocketBaseAltRepository
   *
   * @param pocketBase     PocketBase client
   * @param userRepository User repository for parent relationship
   */
  public PocketBaseAltRepository(PocketBase pocketBase, UserRepository userRepository) {
    this.pocketBase = pocketBase;
    this.gson = new Gson();
    this.userRepository = userRepository;
  }

  @Override
  public Optional<Alt> findById(String id) {
    try {
      JsonObject record = pocketBase.getRecord(COLLECTION_NAME, id, null, null);
      return Optional.of(mapToAlt(record));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error finding alt by ID: " + id, e);
      return Optional.empty();
    }
  }

  @Override
  public List<Alt> findByParentId(String parentId) {
    try {
      String filter = "parent = '" + parentId + "'";
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, filter, null, null);
      return records.stream()
          .map(this::mapToAlt)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error finding alts by parent ID: " + parentId, e);
      return Collections.emptyList();
    }
  }

  @Override
  public Optional<Alt> findByDiscordId(String discordId) {
    try {
      String filter = "discord_id = '" + discordId + "'";
      JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
      return Optional.of(mapToAlt(record));
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "No alt found with Discord ID: " + discordId);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Alt> findBySnowflakeId(Long snowflakeId) {
    return findByDiscordId(snowflakeId.toString());
  }

  @Override
  public Optional<Alt> findByDiscordName(String discordName) {
    try {
      String filter = "discord_name = '" + discordName + "'";
      JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
      return Optional.of(mapToAlt(record));
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "No alt found with Discord name: " + discordName);
      return Optional.empty();
    }
  }

  @Override
  public List<Alt> findAll() {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, null, null, null);
      return records.stream()
          .map(this::mapToAlt)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving all alts", e);
      return Collections.emptyList();
    }
  }

  @Override
  public Alt save(Alt alt) {
    try {
      JsonObject altData = mapToJsonObject(alt);

      if (alt.getId() != null && !alt.getId().isEmpty()) {
        // Update existing alt
        JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, alt.getId(), altData, null, null);
        return mapToAlt(updatedRecord);
      } else {
        // Create new alt
        JsonObject newRecord = pocketBase.createRecord(COLLECTION_NAME, altData, null, null);
        return mapToAlt(newRecord);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error saving alt: " + alt.getDiscord_name(), e);
      throw new RuntimeException("Failed to save alt", e);
    }
  }

  @Override
  public void delete(Alt alt) {
    if (alt.getId() != null) {
      deleteById(alt.getId());
    }
  }

  @Override
  public void deleteById(String id) {
    try {
      pocketBase.deleteRecord(COLLECTION_NAME, id);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error deleting alt with ID: " + id, e);
      throw new RuntimeException("Failed to delete alt", e);
    }
  }

  @Override
  public List<Alt> getAllWhere(String filter) {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, filter, null, null);
      return records.stream()
          .map(this::mapToAlt)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving alts with filter: " + filter, e);
      return Collections.emptyList();
    }
  }

  /**
   * Convert a JsonObject from PocketBase to an Alt object
   *
   * @param json JsonObject from PocketBase API
   * @return Mapped Alt object
   */
  private Alt mapToAlt(JsonObject json) {
    Alt alt = new Alt();

    alt.setId(getStringOrNull(json, "id"));
    alt.setCollectionId(getStringOrNull(json, "collectionId"));
    alt.setCollectionName(getStringOrNull(json, "collectionName"));

    alt.setParent(getStringOrNull(json, "parent"));
    alt.setDiscord_name(getStringOrNull(json, "discord_name"));
    alt.setDiscord_id(getStringOrNull(json, "discord_id"));

    // Handle snowflake ID
    String discordId = getStringOrNull(json, "discord_id");
    if (discordId != null) {
      try {
        alt.setSnowflakeId(Long.parseLong(discordId));
      } catch (NumberFormatException e) {
        LOGGER.warning("Discord ID is not a valid snowflake: " + discordId);
      }
    }

    // Parse datetime fields
    alt.setCreated(getOffsetDateTimeOrNull(json, "created"));
    alt.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

    // Load parent relationship if available
    String parentId = alt.getParent();
    if (parentId != null && userRepository != null) {
      userRepository.findById(parentId).ifPresent(alt::setCachedParent);
    }

    return alt;
  }

  /**
   * Convert an Alt object to a JsonObject for PocketBase
   *
   * @param alt Alt object to convert
   * @return JsonObject for PocketBase API
   */
  private JsonObject mapToJsonObject(Alt alt) {
    JsonObject json = new JsonObject();

    // Only include fields that PocketBase expects for updates/creates
    if (alt.getParent() != null)
      json.addProperty("parent", alt.getParent());
    if (alt.getDiscord_name() != null)
      json.addProperty("discord_name", alt.getDiscord_name());
    if (alt.getDiscord_id() != null)
      json.addProperty("discord_id", alt.getDiscord_id());

    return json;
  }

  // Helper methods for parsing JSON values
  private String getStringOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
  }

  private OffsetDateTime getOffsetDateTimeOrNull(JsonObject json, String key) {
    if (json.has(key) && !json.get(key).isJsonNull()) {
      try {
        String dateStr = json.get(key).getAsString();

        // Check if the string is empty or blank
        if (dateStr == null || dateStr.trim().isEmpty()) {
          return null;
        }

        // Handle PocketBase date format "yyyy-MM-dd HH:mm:ss.SSSZ"
        if (dateStr.contains(" ") && !dateStr.contains("T")) {
          // Replace space with 'T' to make it ISO-8601 compatible
          dateStr = dateStr.replace(" ", "T");
        }

        return OffsetDateTime.parse(dateStr);
      } catch (Exception e) {
        LOGGER.warning("Failed to parse date: " + json.get(key).getAsString() + " - " + e.getMessage());
        try {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
          String dateStr = json.get(key).getAsString();
          if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
          }
          if (dateStr.endsWith("Z")) {
            dateStr = dateStr.substring(0, dateStr.length() - 1) + "+0000";
          }
          return OffsetDateTime.parse(dateStr.replace(" ", "T"));
        } catch (Exception ex) {
          LOGGER.warning("Alternative date parsing also failed: " + ex.getMessage());
          return null;
        }
      }
    }
    return null;
  }
}
