package org.woftnw.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.woftnw.dreamvisitor.data.type.Item;
import org.woftnw.dreamvisitor.data.type.ServerLog;
import org.woftnw.dreamvisitor.pb.PocketBase;

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
 * PocketBase implementation of the ServerRepository interface
 */
public class PocketBaseServerLogsRepository implements ServerLogsRepository {
    private static final Logger LOGGER = Logger.getLogger(PocketBaseServerLogsRepository.class.getName());
    private static final String COLLECTION_NAME = "server_logs";
    private final PocketBase pocketBase;
    private final Gson gson;

    /**
     * Constructor for PocketBaseServerBaseRepository
     *
     * @param pocketBase The PocketBase client to use
     */
    public PocketBaseServerLogsRepository(PocketBase pocketBase) {
        this.pocketBase = pocketBase;
        this.gson = new Gson();
    }

    @Override
    public List<ServerLog> findLast(int count) {
        return List.of();
    }

    @Override
    public ServerLog create(ServerLog log) {
        try {
            JsonObject itemData = mapToJsonObject(log);

            if (log.getId() != null && !log.getId().isEmpty()) {
                // Update existing log
                JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, log.getId(), itemData, null, null);
                return mapToItem(updatedRecord);
            } else {
                // Create new log
                JsonObject newRecord = pocketBase.createRecord(COLLECTION_NAME, itemData, null, null);
                return mapToItem(newRecord);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving log: ", e);
            throw new RuntimeException("Failed to save log", e);
        }
    }

    /**
     * Convert a JsonObject from PocketBase to an Item object
     *
     * @param json JsonObject from PocketBase API
     * @return Mapped Item object
     */
    @NotNull
    private ServerLog mapToItem(JsonObject json) {
        ServerLog log = new ServerLog();

        log.setId(getStringOrNull(json, "id"));
        log.setCollectionId(getStringOrNull(json, "collectionId"));
        log.setCollectionName(getStringOrNull(json, "collectionName"));

        log.setLogMsg(getStringOrNull(json, "log_msg"));

        log.setCreated(getOffsetDateTimeOrNull(json, "created"));
        log.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

        return log;
    }

    /**
     * Convert an Item object to a JsonObject for PocketBase
     *
     * @param log Item object to convert
     * @return JsonObject for PocketBase API
     */
    @NotNull
    private JsonObject mapToJsonObject(@NotNull ServerLog log) {
        JsonObject json = new JsonObject();

        // This is the only field for this type
        if (log.getLogMsg() != null)
            json.addProperty("log_msg", log.getLogMsg());

        return json;
    }
}
