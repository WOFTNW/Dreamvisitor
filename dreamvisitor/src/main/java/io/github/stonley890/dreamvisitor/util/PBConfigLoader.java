package io.github.stonley890.dreamvisitor.util;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.github.stonley890.dreamvisitor.pb.PocketBase;
/**
 * Utility class for loading configuration from PocketBase
 */
public class PBConfigLoader {
  private static final Logger logger = Logger.getLogger("DreamvisitorHub");
  private static final String CONFIG_COLLECTION = "dreamvisitor_config";
  private static final String CONFIG_RECORD_ID = "45q1at367581q3a"; // Default record ID

  private PBConfigLoader() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Loads configuration from PocketBase and returns it as a Map
   *
   * @param pocketBase The PocketBase instance to use
   * @return Map containing the configuration, or empty map if loading fails
   */
  public static Map<String, Object> loadConfig(PocketBase pocketBase) {
    Map<String, Object> config = new HashMap<>();
    try {
      // Fetch the configuration record from PocketBase
      JsonObject configData = pocketBase.getRecord(CONFIG_COLLECTION, CONFIG_RECORD_ID, null, null);

      // Convert JsonObject to Map
      for (String key : configData.keySet()) {
        JsonElement element = configData.get(key);

        // Skip null values - we'll use file config for these
        if (element.isJsonNull()) {
          logger.fine("Skipping null value for key: " + key);
          continue;
        }

        if (element.isJsonPrimitive()) {
          if (element.getAsJsonPrimitive().isNumber()) {
            config.put(key, element.getAsNumber());
          } else if (element.getAsJsonPrimitive().isBoolean()) {
            config.put(key, element.getAsBoolean());
          } else {
            config.put(key, element.getAsString());
          }
        }
      }

      // Rename fields to match the expected format in the existing code
      mapFieldNames(config);

      logger.info("Successfully loaded config from PocketBase");
      return config;
    } catch (IOException e) {
      logger.severe("Failed to load config from PocketBase: " + e.getMessage());
      return new HashMap<>();
    } catch (Exception e) {
      logger.severe("Error parsing config from PocketBase: " + e.getMessage());
      return new HashMap<>();
    }
  }

  /**
   * Maps field names from PocketBase format to the format expected in the code
   *
   * @param config The configuration map to update
   */
  private static void mapFieldNames(Map<String, Object> config) {
    // Map PocketBase field names to the names expected in the application
    renameField(config, "whitelist_channel", "whitelistChannelID");
    renameField(config, "game_chat_channel", "chatChannelID");
    renameField(config, "game_log_channel", "logChannelID");
    renameField(config, "resource_pack_repo", "resourcePackRepo");
    renameField(config, "shop_name", "shopName");
    // Ensure consistent handling of channel IDs
    ensureChannelIdFormat(config, "whitelistChannelID");
    ensureChannelIdFormat(config, "chatChannelID");
    ensureChannelIdFormat(config, "logChannelID");

    // Add other field mappings as needed
  }

  /**
   * Ensures that channel ID fields are consistently stored as Strings
   *
   * @param config    The configuration map
   * @param fieldName The field name to check
   */
  private static void ensureChannelIdFormat(Map<String, Object> config, String fieldName) {
    if (config.containsKey(fieldName)) {
      Object value = config.get(fieldName);
      if (value instanceof Number) {
        // Convert Number to String to ensure consistent handling
        config.put(fieldName, String.valueOf(((Number) value).longValue()));
      }
    }
  }

  /**
   * Renames a field in the configuration map
   *
   * @param config  The configuration map
   * @param oldName The old field name
   * @param newName The new field name
   */
  private static void renameField(Map<String, Object> config, String oldName, String newName) {
    if (config.containsKey(oldName)) {
      config.put(newName, config.get(oldName));
      config.remove(oldName);
    }
  }
}
