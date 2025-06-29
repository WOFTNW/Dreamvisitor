package io.github.stonley890.dreamvisitor.data;

import com.google.gson.JsonObject;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.AutoRestart;
import io.github.stonley890.dreamvisitor.functions.Messager;
import io.github.stonley890.dreamvisitor.pb.PocketBase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PBConfigLoader {
  private static JSONObject config;
  private static String baseUrl;
  private static String configId;
  private static String token;
  private static boolean useRealtime = true;
  private static PocketBase pocketBaseClient;
  private static final String COLLECTION_NAME = "dreamvisitor_config";

  public static void init() {
    FileConfiguration pluginConfig = Dreamvisitor.getPlugin().getConfig();
    baseUrl = pluginConfig.getString("pocketbaseUrl", "http://127.0.0.1:8090/");
    configId = pluginConfig.getString("pocketbaseConfigId", "");
    token = pluginConfig.getString("pocketbaseToken", "");
    useRealtime = pluginConfig.getBoolean("pocketbaseUseRealtime", true);

    if (baseUrl.isEmpty() || configId.isEmpty()) {
      Bukkit.getLogger().warning("PocketBase URL or config ID not configured in plugin config.");
      return;
    }

    // Create PocketBase client from config
    try {
      Map<String, Object> pbConfig = new HashMap<>();
      pbConfig.put("pocketbase-url", baseUrl);
      pbConfig.put("pocketbase-token", token);
      pocketBaseClient = PocketBase.fromConfig(pbConfig);

      Messager.debug("Initialized PocketBase client");
    } catch (Exception e) {
      Bukkit.getLogger().warning("Failed to initialize PocketBase client: " + e.getMessage());
      return;
    }

    // Initial config load
    loadConfig();

    // Start realtime updates if enabled
    if (useRealtime) {
      RealtimeConfigUpdater.init(baseUrl, configId, token);
    }
  }

  public static void loadConfig() {
    try {
      if (pocketBaseClient == null) {
        Dreamvisitor.getPlugin().getLogger().warning("PocketBase client not initialized, cannot load config");
        return;
      }

      // Get record using PocketBase client
      JsonObject record = pocketBaseClient.getRecord(COLLECTION_NAME, configId, null, null);

      // Convert from Gson JsonObject to org.json.JSONObject
      String jsonString = record.toString();
      config = new JSONObject(jsonString);

      Messager.debug("Loaded PocketBase configuration: " + config);

      // Apply config values to the system
      applyConfig();
    } catch (IOException e) {
      Bukkit.getLogger().warning("Error loading PocketBase config: " + e.getMessage());
    }
  }

  public static void updateLocalConfig(JSONObject newConfigData) {
    // Update our local config with new data
    if (config != null) {
      // Merge the new data into our existing config
      for (String key : newConfigData.keySet()) {
        config.put(key, newConfigData.get(key));
      }

      // Apply the updated config
      applyConfig();
    }
  }

  private static void applyConfig() {
    // Apply configuration values to the relevant systems
    if (config != null) {
      // Handle autoRestart setting
      if (config.has("autoRestart")) {
        boolean autoRestart = config.getBoolean("autoRestart");
        if (autoRestart != AutoRestart.isAutoRestart()) {
          if (autoRestart) {
            AutoRestart.enableAutoRestart(null);
            Messager.debug("Auto restart enabled from remote config");
          } else {
            AutoRestart.disableAutoRestart();
            Messager.debug("Auto restart disabled from remote config");
          }
        }
      }

      // Add more configuration handlers here as needed
    }
  }

  @NotNull
  @Contract("_, _ -> new")
  public static CompletableFuture<Void> updateConfigField(String field, boolean value) {
    return CompletableFuture.runAsync(() -> {
      try {
        if (pocketBaseClient == null) {
          Bukkit.getLogger().warning("PocketBase client not initialized, cannot update config");
          return;
        }

        // Create update data object
        JsonObject updateData = new JsonObject();
        updateData.addProperty(field, value);

        // Update the record
        pocketBaseClient.updateRecord(COLLECTION_NAME, configId, updateData, null, null);

        Messager.debug("Updated PocketBase configuration field " + field + " to " + value);

        // If not using realtime updates, we need to reload config manually
        if (!useRealtime) {
          loadConfig();
        }
      } catch (IOException e) {
        Dreamvisitor.getPlugin().getLogger().warning("Error updating PocketBase config: " + e.getMessage());
      }
    });
  }

  public static boolean getBoolean(String field, boolean defaultValue) {
    if (config == null) {
      return defaultValue;
    }

    try {
      return config.getBoolean(field);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public static PocketBase getClient() {
    return pocketBaseClient;
  }
}
