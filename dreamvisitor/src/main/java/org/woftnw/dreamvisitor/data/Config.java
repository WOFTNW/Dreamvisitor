package org.woftnw.dreamvisitor.data;

import com.google.gson.JsonObject;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.functions.AutoRestart;
import org.woftnw.dreamvisitor.functions.Messager;
import org.woftnw.dreamvisitor.pb.PocketBase;
import org.woftnw.dreamvisitor.util.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Config {
    private static JSONObject config;
    private static String baseUrl;
    private static String configId;
    private static String token;
    private static boolean useRealtime = true;
    private static PocketBase pocketBaseClient;
    private static final String COLLECTION_NAME = "dreamvisitor_config";

    public static void init(PocketBase pocketBase, String baseUrl, String configId, String token, boolean useRealtime) throws IOException {

        pocketBaseClient = pocketBase;
        Config.baseUrl = baseUrl;
        Config.configId = configId;
        Config.token = token;
        Config.useRealtime = useRealtime;

        // Initial config load
        loadConfig();

        // Start realtime updates if enabled
        if (useRealtime) {
            RealtimeConfigUpdater.init(baseUrl, configId, token);
        }
    }

    public static void loadConfig() throws IOException {
        if (pocketBaseClient == null) {
            Dreamvisitor.getPlugin().getLogger().warning("PocketBase client not initialized, cannot load config");
            return;
        }

        // Get record using PocketBase client
        Dreamvisitor.getPlugin().getLogger().info("Getting record " + configId + " from collection " + COLLECTION_NAME);
        JsonObject record = pocketBaseClient.getRecord(COLLECTION_NAME, configId, null, null);

        // Convert from Gson JsonObject to org.json.JSONObject
        String jsonString = record.toString();
        config = new JSONObject(jsonString);

        Dreamvisitor.getPlugin().getLogger().info("Loaded PocketBase configuration: " + config);

        // Apply config values to the system
        applyConfig();
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
    public static <T> CompletableFuture<Void> set(ConfigKey key, T value) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (pocketBaseClient == null) {
                    Dreamvisitor.getPlugin().getLogger().warning("PocketBase client not initialized, cannot update config");
                    return;
                }

                if (value != null && !key.getType().isInstance(value)) {
                    throw new IllegalArgumentException("Value for config key '" + key.getKey() +
                            "' is not of expected type: " + key.getType().getSimpleName());
                }

                // Create update data object
                JsonObject updateData = new JsonObject();

                // Handle manual conversion for supported types
                if (value instanceof Boolean) {
                    updateData.addProperty(key.getKey(), (Boolean) value);
                } else if (value instanceof Number) {
                    updateData.addProperty(key.getKey(), (Number) value);
                } else if (value instanceof String) {
                    updateData.addProperty(key.getKey(), (String) value);
                } else {
                    // More types can be added if needed
                    throw new IllegalArgumentException("Unsupported config value type for key '" + key.getKey() + "'");
                }

                // Update the record
                pocketBaseClient.updateRecord(COLLECTION_NAME, configId, updateData, null, null);

                Messager.debug("Updated PocketBase configuration field " + key.getKey() + " to " + value);

                // Update local storage
                config.put(key.getKey(), value);

                // If not using realtime updates, we need to reload config manually
                if (!useRealtime) {
                    loadConfig();
                }
            } catch (IOException e) {
                Dreamvisitor.getPlugin().getLogger().warning("Error updating PocketBase config: " + e.getMessage());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(@NotNull ConfigKey configKey) {
        Object value = config.get(configKey.getKey());

        if (value == null) {
            value = configKey.getDefaultValue();
        }

        Class<?> expectedType = configKey.getType();

        // Handle numeric coercion
        if (expectedType == Double.class && value instanceof Number) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        } else if (expectedType == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        } else if (expectedType == Float.class && value instanceof Number) {
            return (T) Float.valueOf(((Number) value).floatValue());
        } else if (expectedType.isInstance(value)) {
            return (T) value;
        }

        throw new IllegalStateException("Config value for key '" + configKey.getKey() +
                "' is not of expected type: " + configKey.getType().getSimpleName());
    }

}
