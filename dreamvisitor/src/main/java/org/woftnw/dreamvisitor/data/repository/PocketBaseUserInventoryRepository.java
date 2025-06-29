package org.woftnw.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.data.type.Item;
import org.woftnw.dreamvisitor.data.type.UserInventory;
import org.woftnw.dreamvisitor.pb.PocketBase;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PocketBase implementation of the UserInventoryRepository interface
 */
public class PocketBaseUserInventoryRepository implements UserInventoryRepository {
  private static final Logger LOGGER = Logger.getLogger(PocketBaseUserInventoryRepository.class.getName());
  private static final String COLLECTION_NAME = "user_inventory";
  private final PocketBase pocketBase;
  private final Gson gson;
  private final UserRepository userRepository;
  private final ItemRepository itemRepository;

  /**
   * Constructor for PocketBaseUserInventoryRepository
   *
   * @param pocketBase     The PocketBase client to use
   * @param userRepository The user repository for fetching related users
   * @param itemRepository The item repository for fetching related items
   */
  public PocketBaseUserInventoryRepository(PocketBase pocketBase, UserRepository userRepository,
      ItemRepository itemRepository) {
    this.pocketBase = pocketBase;
    this.gson = new Gson();
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
  }

  @Override
  public Optional<UserInventory> findById(String id) {
    try {
      JsonObject record = pocketBase.getRecord(COLLECTION_NAME, id, null, null);
      return Optional.of(mapToUserInventory(record));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error finding user inventory by ID: " + id, e);
      return Optional.empty();
    }
  }

  @Override
  public List<UserInventory> findByUser(String userId) {
    try {
      String filter = "user = '" + userId + "'";
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, filter, null, null);
      List<UserInventory> inventoryEntries = records.stream()
          .map(this::mapToUserInventory)
          .collect(Collectors.toList());

      // Load related items if there are inventory entries
      if (!inventoryEntries.isEmpty()) {
        loadRelatedItems(inventoryEntries);
      }

      return inventoryEntries;
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving user inventory for user: " + userId, e);
      return Collections.emptyList();
    }
  }

  @Override
  public Optional<UserInventory> findByUserAndItem(String userId, String itemId) {
    try {
      String filter = "user = '" + userId + "' && item = '" + itemId + "'";
      JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
      UserInventory inventory = mapToUserInventory(record);

      // Load related item and user
      if (inventory.getItem() != null) {
        itemRepository.findById(inventory.getItem()).ifPresent(inventory::setCachedItem);
      }
      if (inventory.getUser() != null) {
        userRepository.findById(inventory.getUser()).ifPresent(inventory::setCachedUser);
      }

      return Optional.of(inventory);
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "No inventory entry found for user: " + userId + " and item: " + itemId);
      return Optional.empty();
    }
  }

  @Override
  public List<UserInventory> findAll() {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, null, null, null);
      return records.stream()
          .map(this::mapToUserInventory)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving all user inventory entries", e);
      return Collections.emptyList();
    }
  }

  @Override
  public UserInventory save(UserInventory userInventory) {
    try {
      JsonObject inventoryData = mapToJsonObject(userInventory);

      if (userInventory.getId() != null && !userInventory.getId().isEmpty()) {
        // Update existing inventory entry
        JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, userInventory.getId(), inventoryData, null,
            null);
        return mapToUserInventory(updatedRecord);
      } else {
        // Create new inventory entry
        JsonObject newRecord = pocketBase.createRecord(COLLECTION_NAME, inventoryData, null, null);
        return mapToUserInventory(newRecord);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error saving user inventory entry: " + userInventory.getId(), e);
      throw new RuntimeException("Failed to save user inventory entry", e);
    }
  }

  @Override
  public void delete(UserInventory userInventory) {
    if (userInventory.getId() != null) {
      deleteById(userInventory.getId());
    }
  }

  @Override
  public void deleteById(String id) {
    try {
      pocketBase.deleteRecord(COLLECTION_NAME, id);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error deleting user inventory entry with ID: " + id, e);
      throw new RuntimeException("Failed to delete user inventory entry", e);
    }
  }

  @Override
  public List<UserInventory> getAllWhere(String filter) {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, filter, null, null, null);
      return records.stream()
          .map(this::mapToUserInventory)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving user inventory entries with filter: " + filter, e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<UserInventory> loadRelatedItems(List<UserInventory> inventoryEntries) {
    // Get unique item IDs
    Set<String> itemIds = inventoryEntries.stream()
        .map(UserInventory::getItem)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    // Fetch all items in one go
    Map<String, Item> itemMap = new HashMap<>();
    for (String itemId : itemIds) {
      itemRepository.findById(itemId).ifPresent(item -> itemMap.put(itemId, item));
    }

    // Set cached items
    for (UserInventory entry : inventoryEntries) {
      if (entry.getItem() != null && itemMap.containsKey(entry.getItem())) {
        entry.setCachedItem(itemMap.get(entry.getItem()));
      }
    }

    return inventoryEntries;
  }

  @Override
  public List<UserInventory> loadRelatedUsers(List<UserInventory> inventoryEntries) {
    // Get unique user IDs
    Set<String> userIds = inventoryEntries.stream()
        .map(UserInventory::getUser)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    // Fetch all users in one go
    Map<String, DVUser> userMap = new HashMap<>();
    for (String userId : userIds) {
      userRepository.findById(userId).ifPresent(user -> userMap.put(userId, user));
    }

    // Set cached users
    for (UserInventory entry : inventoryEntries) {
      if (entry.getUser() != null && userMap.containsKey(entry.getUser())) {
        entry.setCachedUser(userMap.get(entry.getUser()));
      }
    }

    return inventoryEntries;
  }

  /**
   * Convert a JsonObject from PocketBase to a UserInventory object
   *
   * @param json JsonObject from PocketBase API
   * @return Mapped UserInventory object
   */
  private UserInventory mapToUserInventory(JsonObject json) {
    UserInventory inventory = new UserInventory();

    inventory.setId(getStringOrNull(json, "id"));
    inventory.setCollectionId(getStringOrNull(json, "collectionId"));
    inventory.setCollectionName(getStringOrNull(json, "collectionName"));

    inventory.setUser(getStringOrNull(json, "user"));
    inventory.setItem(getStringOrNull(json, "item"));
    inventory.setQuantity(getIntOrNull(json, "quantity"));

    inventory.setCreated(getOffsetDateTimeOrNull(json, "created"));
    inventory.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

    return inventory;
  }

  /**
   * Convert a UserInventory object to a JsonObject for PocketBase
   *
   * @param inventory UserInventory object to convert
   * @return JsonObject for PocketBase API
   */
  private JsonObject mapToJsonObject(UserInventory inventory) {
    JsonObject json = new JsonObject();

    // Only include fields that PocketBase expects for updates/creates
    if (inventory.getUser() != null)
      json.addProperty("user", inventory.getUser());
    if (inventory.getItem() != null)
      json.addProperty("item", inventory.getItem());
    if (inventory.getQuantity() != null)
      json.addProperty("quantity", inventory.getQuantity());

    return json;
  }

  // Helper methods for extracting values from JsonObject
  private String getStringOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
  }

  private Integer getIntOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
  }

  private Double getDoubleOrNull(JsonObject json, String key) {
    return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsDouble() : null;
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
