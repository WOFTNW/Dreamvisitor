package org.woftnw.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.pb.PocketBase;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PocketBase implementation of the InfractionRepository interface
 */
public class PocketBaseInfractionRepository implements InfractionRepository {
  private static final Logger LOGGER = Logger.getLogger(PocketBaseInfractionRepository.class.getName());
  private static final String COLLECTION_NAME = "infractions";
  private final PocketBase pocketBase;
  private final Gson gson;
  private final UserRepository userRepository;

  /**
   * Constructor for PocketBaseInfractionRepository
   *
   * @param pocketBase     The PocketBase client to use
   * @param userRepository The user repository for fetching related users
   */
  public PocketBaseInfractionRepository(PocketBase pocketBase, UserRepository userRepository) {
    this.pocketBase = pocketBase;
    this.gson = new Gson();
    this.userRepository = userRepository;
  }

  @Override
  public Optional<Infraction> findById(String id) {
    try {
      JsonObject record = pocketBase.getRecord(COLLECTION_NAME, id, null, null);
      return Optional.of(mapToInfraction(record));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error finding infraction by ID: " + id, e);
      return Optional.empty();
    }
  }

  @Override
  public List<Infraction> findByUser(String userId) {
    try {
      String filter = "user = '" + userId + "'";
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, filter, null, null);
      List<Infraction> infractions = records.stream()
          .map(this::mapToInfraction)
          .collect(Collectors.toList());

      // Load related users if there are infractions
      if (!infractions.isEmpty()) {
        loadRelatedUsers(infractions);
      }

      return infractions;
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving infractions for user: " + userId, e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<Infraction> findActiveByUser(String userId) {
    try {
      String filter = "user = '" + userId + "' && expired = false";
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, filter, null, null);
      List<Infraction> infractions = records.stream()
          .map(this::mapToInfraction)
          .collect(Collectors.toList());

      // Load related users if there are infractions
      if (!infractions.isEmpty()) {
        loadRelatedUsers(infractions);
      }

      return infractions;
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving active infractions for user: " + userId, e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<Infraction> findAll() {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, null, null, null);
      return records.stream()
          .map(this::mapToInfraction)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving all infractions", e);
      return Collections.emptyList();
    }
  }

  @Override
  public Infraction save(Infraction infraction) {
    try {
      JsonObject infractionData = mapToJsonObject(infraction);

      if (infraction.getId() != null && !infraction.getId().isEmpty()) {
        // Update existing infraction
        JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, infraction.getId(), infractionData, null,
            null);
        return mapToInfraction(updatedRecord);
      } else {
        // Create new infraction
        JsonObject newRecord = pocketBase.createRecord(COLLECTION_NAME, infractionData, null, null);
        return mapToInfraction(newRecord);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error saving infraction: " + infraction.getId(), e);
      throw new RuntimeException("Failed to save infraction", e);
    }
  }

  @Override
  public void delete(Infraction infraction) {
    if (infraction.getId() != null) {
      deleteById(infraction.getId());
    }
  }

  @Override
  public void deleteById(String id) {
    try {
      pocketBase.deleteRecord(COLLECTION_NAME, id);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error deleting infraction with ID: " + id, e);
      throw new RuntimeException("Failed to delete infraction", e);
    }
  }

  @Override
  public List<Infraction> getAllWhere(String filter) {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, filter, null, null, null);
      return records.stream()
          .map(this::mapToInfraction)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving infractions with filter: " + filter, e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<Infraction> loadRelatedUsers(List<Infraction> infractions) {
    // Get unique user IDs
    Set<String> userIds = infractions.stream()
        .map(Infraction::getUser)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    // Fetch all users in one go
    Map<String, DVUser> userMap = new HashMap<>();
    for (String userId : userIds) {
      userRepository.findById(userId).ifPresent(user -> userMap.put(userId, user));
    }

    // Set cached users
    for (Infraction infraction : infractions) {
      if (infraction.getUser() != null && userMap.containsKey(infraction.getUser())) {
        infraction.setCachedUser(userMap.get(infraction.getUser()));
      }
    }

    return infractions;
  }

  /**
   * Convert a JsonObject from PocketBase to an Infraction object
   *
   * @param json JsonObject from PocketBase API
   * @return Mapped Infraction object
   */
  private Infraction mapToInfraction(JsonObject json) {
    Infraction infraction = new Infraction();

    infraction.setId(getStringOrNull(json, "id"));
    infraction.setCollectionId(getStringOrNull(json, "collectionId"));
    infraction.setCollectionName(getStringOrNull(json, "collectionName"));

    infraction.setReason(getStringOrNull(json, "reason"));
    infraction.setSend_warning(getBooleanOrNull(json, "send_warning"));
    infraction.setExpired(getBooleanOrNull(json, "expired"));
    infraction.setValue(getIntOrNull(json, "value"));
    infraction.setUser(getStringOrNull(json, "user"));

    infraction.setCreated(getOffsetDateTimeOrNull(json, "created"));
    infraction.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

    return infraction;
  }

  /**
   * Convert an Infraction object to a JsonObject for PocketBase
   *
   * @param infraction Infraction object to convert
   * @return JsonObject for PocketBase API
   */
  private JsonObject mapToJsonObject(Infraction infraction) {
    JsonObject json = new JsonObject();

    // Only include fields that PocketBase expects for updates/creates
    if (infraction.getReason() != null)
      json.addProperty("reason", infraction.getReason());
    if (infraction.getSend_warning() != null)
      json.addProperty("send_warning", infraction.getSend_warning());
    if (infraction.getExpired() != null)
      json.addProperty("expired", infraction.getExpired());
    if (infraction.getValue() != null)
      json.addProperty("value", infraction.getValue());
    if (infraction.getUser() != null)
      json.addProperty("user", infraction.getUser());

    return json;
  }

  // Helper methods for extracting values from JsonObject
  private String getStringOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
  }

  private Integer getIntOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
  }

  private Boolean getBooleanOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : null;
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

        // Try alternative parsing with explicit formatter
        try {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
          String dateStr = json.get(key).getAsString();

          // Check if the string is empty or blank
          if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
          }

          // Convert to ISO format for parsing
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

  private String formatDateTime(OffsetDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
}
