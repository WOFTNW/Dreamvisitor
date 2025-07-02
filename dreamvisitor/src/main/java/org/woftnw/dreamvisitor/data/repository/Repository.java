package org.woftnw.dreamvisitor.data.repository;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.woftnw.dreamvisitor.Dreamvisitor;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public interface Repository {

    // Helper methods for extracting values from JsonObject
    @Nullable
    default String getStringOrNull(@NotNull JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }

        com.google.gson.JsonElement element = json.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        } else {
            Dreamvisitor.getPlugin().getLogger().warning("Field " + key + " is not a string primitive: " + element);
            return null;
        }
    }

    @Nullable
    default Integer getIntOrNull(@NotNull JsonObject json, String key) {
        try {
            return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    default Long getLongOrNull(@NotNull JsonObject json, String key) {
        try {
            return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsLong() : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    default Double getDoubleOrNull(@NotNull JsonObject json, String key) {
        try {
            return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsDouble() : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    default Boolean getBooleanOrNull(@NotNull JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : null;
    }

    @Nullable
    default OffsetDateTime getOffsetDateTimeOrNull(@NotNull JsonObject json, String key) {
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
                Dreamvisitor.getPlugin().getLogger().warning("Failed to parse date: " + json.get(key).getAsString() + " - " + e.getMessage());

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
                    Dreamvisitor.getPlugin().getLogger().warning("Alternative date parsing also failed: " + ex.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    @NotNull
    default String formatDateTime(@NotNull OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Safely get a JSON array field as a JSON string
     * Handles cases where the field could be a string, array, or other type
     */
    @Nullable
    default String getJsonArrayAsString(@NotNull JsonObject json, String key) {
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
            Dreamvisitor.getPlugin().getLogger().warning("Field " + key + " is not a string or array: " + element);
            return element.toString();
        }
    }

}
