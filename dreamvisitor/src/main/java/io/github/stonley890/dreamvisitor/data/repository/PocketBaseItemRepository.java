package io.github.stonley890.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.stonley890.dreamvisitor.data.type.Item;
import io.github.stonley890.dreamvisitor.pb.PocketBase;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PocketBase implementation of the ItemRepository interface
 */
public class PocketBaseItemRepository implements ItemRepository {
  private static final Logger LOGGER = Logger.getLogger(PocketBaseItemRepository.class.getName());
  private static final String COLLECTION_NAME = "items";
  private final PocketBase pocketBase;
  private final Gson gson;

  /**
   * Constructor for PocketBaseItemRepository
   *
   * @param pocketBase The PocketBase client to use
   */
  public PocketBaseItemRepository(PocketBase pocketBase) {
    this.pocketBase = pocketBase;
    this.gson = new Gson();
  }

  @Override
  public Optional<Item> findById(String id) {
    try {
      JsonObject record = pocketBase.getRecord(COLLECTION_NAME, id, null, null);
      return Optional.of(mapToItem(record));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error finding item by ID: " + id, e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Item> findByName(String name) {
    try {
      String filter = "name = '" + name + "'";
      JsonObject record = pocketBase.getFirstListItem(COLLECTION_NAME, filter, null, null, null);
      return Optional.of(mapToItem(record));
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "No item found with name: " + name);
      return Optional.empty();
    }
  }

  @Override
  public List<Item> findAll() {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, null, null, null);
      return records.stream()
          .map(this::mapToItem)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving all items", e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<Item> findAllEnabled() {
    try {
      String filter = "enabled = true";
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, null, filter, null, null);
      return records.stream()
          .map(this::mapToItem)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving enabled items", e);
      return Collections.emptyList();
    }
  }

  @Override
  public Item save(Item item) {
    try {
      JsonObject itemData = mapToJsonObject(item);

      if (item.getId() != null && !item.getId().isEmpty()) {
        // Update existing item
        JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, item.getId(), itemData, null, null);
        return mapToItem(updatedRecord);
      } else {
        // Create new item
        JsonObject newRecord = pocketBase.createRecord(COLLECTION_NAME, itemData, null, null);
        return mapToItem(newRecord);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error saving item: " + item.getName(), e);
      throw new RuntimeException("Failed to save item", e);
    }
  }

  @Override
  public void delete(Item item) {
    if (item.getId() != null) {
      deleteById(item.getId());
    }
  }

  @Override
  public void deleteById(String id) {
    try {
      pocketBase.deleteRecord(COLLECTION_NAME, id);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error deleting item with ID: " + id, e);
      throw new RuntimeException("Failed to delete item", e);
    }
  }

  @Override
  public List<Item> getAllWhere(String filter) {
    try {
      List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, 500, filter, null, null, null);
      return records.stream()
          .map(this::mapToItem)
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error retrieving items with filter: " + filter, e);
      return Collections.emptyList();
    }
  }

  /**
   * Convert a JsonObject from PocketBase to an Item object
   *
   * @param json JsonObject from PocketBase API
   * @return Mapped Item object
   */
  private Item mapToItem(JsonObject json) {
    Item item = new Item();

    item.setId(getStringOrNull(json, "id"));
    item.setCollectionId(getStringOrNull(json, "collectionId"));
    item.setCollectionName(getStringOrNull(json, "collectionName"));

    item.setName(getStringOrNull(json, "name"));
    item.setDescription(getStringOrNull(json, "description"));
    item.setPrice(getDoubleOrNull(json, "price"));
    item.setSale_percent(getDoubleOrNull(json, "sale_percent"));
    item.setQuantity(getIntOrNull(json, "quantity"));
    item.setGifting_enabled(getBooleanOrNull(json, "gifting_enabled"));
    item.setEnabled(getBooleanOrNull(json, "enabled"));
    item.setMax_allowed(getIntOrNull(json, "max_allowed"));
    item.setUse_disabled(getBooleanOrNull(json, "use_disabled"));
    item.setUse_on_purchase(getBooleanOrNull(json, "use_on_purchase"));

    // For JSON fields that could be arrays, use getJsonArrayAsString
    item.setOn_use_groups_add(getJsonArrayAsString(json, "on_use_groups_add"));
    item.setOn_use_groups_remove(getJsonArrayAsString(json, "on_use_groups_remove"));
    item.setOn_use_roles_add(getJsonArrayAsString(json, "on_use_roles_add"));
    item.setOn_use_roles_remove(getJsonArrayAsString(json, "on_use_roles_remove"));
    item.setOn_use_console_commands(getJsonArrayAsString(json, "on_use_console_commands"));

    item.setCreated(getOffsetDateTimeOrNull(json, "created"));
    item.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

    return item;
  }

  /**
   * Convert an Item object to a JsonObject for PocketBase
   *
   * @param item Item object to convert
   * @return JsonObject for PocketBase API
   */
  private JsonObject mapToJsonObject(Item item) {
    JsonObject json = new JsonObject();

    // Only include fields that PocketBase expects for updates/creates
    if (item.getName() != null)
      json.addProperty("name", item.getName());
    if (item.getDescription() != null)
      json.addProperty("description", item.getDescription());

    // Always include price even if it's 0.0 - PocketBase requires this field
    // Previously we only included it if not null, but 0.0 might still be included
    // in the JSON
    if (item.getPrice() != null) {
      json.addProperty("price", item.getPrice());
    } else {
      // Set a default price of 0.0 if null to prevent validation errors
      json.addProperty("price", 0.0);
    }

    // For all other optional fields, only include if not null
    if (item.getSale_percent() != null)
      json.addProperty("sale_percent", item.getSale_percent());
    if (item.getQuantity() != null)
      json.addProperty("quantity", item.getQuantity());
    if (item.getGifting_enabled() != null)
      json.addProperty("gifting_enabled", item.getGifting_enabled());
    if (item.getEnabled() != null)
      json.addProperty("enabled", item.getEnabled());
    if (item.getMax_allowed() != null)
      json.addProperty("max_allowed", item.getMax_allowed());
    if (item.getUse_disabled() != null)
      json.addProperty("use_disabled", item.getUse_disabled());
    if (item.getUse_on_purchase() != null)
      json.addProperty("use_on_purchase", item.getUse_on_purchase());
    if (item.getOn_use_groups_add() != null)
      json.addProperty("on_use_groups_add", item.getOn_use_groups_add());
    if (item.getOn_use_groups_remove() != null)
      json.addProperty("on_use_groups_remove", item.getOn_use_groups_remove());
    if (item.getOn_use_roles_add() != null)
      json.addProperty("on_use_roles_add", item.getOn_use_roles_add());
    if (item.getOn_use_roles_remove() != null)
      json.addProperty("on_use_roles_remove", item.getOn_use_roles_remove());
    if (item.getOn_use_console_commands() != null)
      json.addProperty("on_use_console_commands", item.getOn_use_console_commands());

    return json;
  }

  // Helper methods for extracting values from JsonObject
  private String getStringOrNull(JsonObject json, String key) {
    if (!json.has(key) || json.get(key).isJsonNull()) {
      return null;
    }

    com.google.gson.JsonElement element = json.get(key);
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
      return element.getAsString();
    } else {
      LOGGER.warning("Field " + key + " is not a string primitive: " + element);
      return null;
    }
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

  /**
   * Safely get a JSON array field as a JSON string
   * Handles cases where the field could be a string, array, or other type
   */
  private String getJsonArrayAsString(JsonObject json, String key) {
    if (!json.has(key) || json.get(key).isJsonNull()) {
      return null;
    }

    com.google.gson.JsonElement element = json.get(key);
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
      // It's already a string, return it directly
      return element.getAsString();
    } else if (element.isJsonArray()) {
      // It's an array, convert to a well-formatted JSON string
      return element.toString();
    } else {
      // For any other type, convert to string representation
      LOGGER.warning("Field " + key + " is not a string or array: " + element);
      return element.toString();
    }
  }
}
