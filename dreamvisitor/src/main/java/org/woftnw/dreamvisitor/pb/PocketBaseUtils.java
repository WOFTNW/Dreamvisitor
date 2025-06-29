package org.woftnw.dreamvisitor.pb;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for PocketBase operations
 */
public class PocketBaseUtils {
  private static final Gson gson = new Gson();

  /**
   * Converts a JsonObject to a specific class instance
   *
   * @param json  The JsonObject to convert
   * @param clazz The target class
   * @param <T>   Target type
   * @return An instance of the target class
   */
  public static <T> T jsonToClass(@NotNull JsonObject json, @NotNull Class<T> clazz) {
    return gson.fromJson(json, clazz);
  }

  /**
   * Converts a JsonObject to an instance of the specified type
   *
   * @param json The JsonObject to convert
   * @param type The target type
   * @param <T>  Target type
   * @return An instance of the target type
   */
  public static <T> T jsonToType(@NotNull JsonObject json, @NotNull Type type) {
    return gson.fromJson(json, type);
  }

  /**
   * Converts a list of JsonObjects to a list of class instances
   *
   * @param jsonList The list of JsonObjects
   * @param clazz    The target class
   * @param <T>      Target type
   * @return A list of instances of the target class
   */
  public static <T> List<T> jsonListToClass(@NotNull List<JsonObject> jsonList, @NotNull Class<T> clazz) {
    return jsonList.stream()
        .map(json -> jsonToClass(json, clazz))
        .collect(Collectors.toList());
  }

  /**
   * Gets a string value from JsonObject safely
   *
   * @param json JsonObject to get value from
   * @param key  Key to lookup
   * @return String value or null if not found or not a string
   */
  public static String getString(JsonObject json, String key) {
    if (json == null || !json.has(key))
      return null;
    JsonElement element = json.get(key);
    return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
        ? element.getAsString()
        : null;
  }

  /**
   * Gets an integer value from JsonObject safely
   *
   * @param json JsonObject to get value from
   * @param key  Key to lookup
   * @return Integer value or null if not found or not a number
   */
  public static Integer getInteger(JsonObject json, String key) {
    if (json == null || !json.has(key))
      return null;
    JsonElement element = json.get(key);
    return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
        ? element.getAsInt()
        : null;
  }

  /**
   * Gets a double value from JsonObject safely
   *
   * @param json JsonObject to get value from
   * @param key  Key to lookup
   * @return Double value or null if not found or not a number
   */
  public static Double getDouble(JsonObject json, String key) {
    if (json == null || !json.has(key))
      return null;
    JsonElement element = json.get(key);
    return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
        ? element.getAsDouble()
        : null;
  }

  /**
   * Gets a boolean value from JsonObject safely
   *
   * @param json JsonObject to get value from
   * @param key  Key to lookup
   * @return Boolean value or null if not found or not a boolean
   */
  public static Boolean getBoolean(JsonObject json, String key) {
    if (json == null || !json.has(key))
      return null;
    JsonElement element = json.get(key);
    return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
        ? element.getAsBoolean()
        : null;
  }

  /**
   * Gets a nested JsonObject safely
   *
   * @param json JsonObject to get value from
   * @param key  Key to lookup
   * @return JsonObject or null if not found or not an object
   */
  public static JsonObject getJsonObject(JsonObject json, String key) {
    if (json == null || !json.has(key))
      return null;
    JsonElement element = json.get(key);
    return element.isJsonObject() ? element.getAsJsonObject() : null;
  }
}
