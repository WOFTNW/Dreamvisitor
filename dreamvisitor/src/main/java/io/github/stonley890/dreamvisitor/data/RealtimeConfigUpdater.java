package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.Bukkit;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class RealtimeConfigUpdater {
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();
  private static String baseUrl;
  private static String configId;
  private static String token;
  private static final String collectionName = "dreamvisitor_config";
  private static String clientId;
  private static final AtomicBoolean isRunning = new AtomicBoolean(false);
  private static final AtomicBoolean isConnecting = new AtomicBoolean(false);
  private static int retryCount = 0;
  private static final int MAX_RETRIES = 5;
  private static final int RETRY_DELAY_MS = 3000; // 5 seconds between retries

  public static void init(String pbBaseUrl, String pbConfigId, String pbToken) {
    baseUrl = pbBaseUrl;
    configId = pbConfigId;
    token = pbToken;
    startRealtimeUpdates();
  }

  public static void startRealtimeUpdates() {
    if (isConnecting.get() || isRunning.get()) {
      return;
    }

    if (baseUrl == null || baseUrl.isEmpty() || configId == null || configId.isEmpty()) {
      Messager.debug("Cannot start realtime updates: baseUrl or configId is not set");
      return;
    }

    isConnecting.set(true);

    CompletableFuture.runAsync(() -> {
      try {
        connectSSE();
      } catch (Exception e) {
        Messager.debug("Error in SSE connection: " + e.getMessage());
        handleReconnect();
      } finally {
        isConnecting.set(false);
      }
    }, executor);
  }

  private static void connectSSE() throws IOException {
    Messager.debug("Connecting to SSE endpoint");

    URL url = new URL(baseUrl + "/api/realtime");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setReadTimeout(6000); // 1 minute timeout

    // Add authentication if token is provided
    if (token != null && !token.isEmpty()) {
      connection.setRequestProperty("Authorization", "Bearer " + token);
    }

    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        isRunning.set(true);
        retryCount = 0;

        String line;
        while ((line = reader.readLine()) != null && isRunning.get()) {
          if (line.startsWith("event: PB_CONNECT")) {
            // Next line should be "data: {clientId}"
            line = reader.readLine();
            if (line != null && line.startsWith("data: ")) {
              clientId = line.substring(6).trim();
              Messager.debug("Connected to SSE with client ID: " + clientId);
              setSubscription();
            }
          } else if (line.startsWith("event: PB_DISCONNECT")) {
            Messager.debug("Received disconnect event from server");
            break;
          } else if (line.startsWith("event: update")) {
            // Next line should be data
            line = reader.readLine();
            if (line != null && line.startsWith("data: ")) {
              handleUpdateEvent(line.substring(6));
            }
          }
        }
      } finally {
        isRunning.set(false);
        if (!isConnecting.get()) {
          handleReconnect();
        }
      }
    } else {
      throw new IOException("Failed to connect to SSE endpoint: " + responseCode);
    }
  }

  private static void setSubscription() {
    try {
      URL url = new URL(baseUrl + "/api/realtime");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");

      // Add authentication if token is provided
      if (token != null && !token.isEmpty()) {
        connection.setRequestProperty("Authorization", "Bearer " + token);
      }

      connection.setDoOutput(true);

      // Subscribe to collection/record
      JSONObject requestBody = new JSONObject();
      requestBody.put("clientId", clientId);
      requestBody.put("subscriptions", new String[] { collectionName + "/" + configId });

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        Messager.debug("Failed to set subscription: " + responseCode);

        // Read error response if available
        if (connection.getErrorStream() != null) {
          try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
              errorResponse.append(line);
            }
            Messager.debug("Error response: " + errorResponse);
          }
        }
      } else {
        Messager.debug("Successfully subscribed to config updates");
      }
    } catch (Exception e) {
      Messager.debug("Error setting subscription: " + e.getMessage());
    }
  }

  private static void handleUpdateEvent(String data) {
    try {
      JSONObject jsonData = new JSONObject(data);
      if (jsonData.has("record")) {
        JSONObject record = jsonData.getJSONObject("record");

        // Handle autoRestart field
        if (record.has("autoRestart")) {
          boolean autoRestart = record.getBoolean("autoRestart");
          Messager.debug("Received real-time update: autoRestart = " + autoRestart);

          // Schedule update on main thread
          Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
            Config.updateLocalConfig(record);
          });
        }
      }
    } catch (JSONException e) {
      Messager.debug("Error parsing update event: " + e.getMessage());
    }
  }

  private static void handleReconnect() {
    if (retryCount < MAX_RETRIES) {
      retryCount++;
      Messager.debug("Attempting to reconnect SSE (attempt " + retryCount + "/" + MAX_RETRIES + ")");

      try {
        Thread.sleep(RETRY_DELAY_MS);
        startRealtimeUpdates();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } else {
      Messager.debug("Max retry attempts reached. Giving up on SSE connection.");
      // Fallback to polling
      retryCount = 0;
    }
  }

  public static void shutdown() {
    isRunning.set(false);
    executor.shutdown();
  }
}
