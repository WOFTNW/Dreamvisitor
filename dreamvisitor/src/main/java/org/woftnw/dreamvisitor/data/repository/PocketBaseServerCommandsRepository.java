package org.woftnw.dreamvisitor.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.data.type.ServerCommand;
import org.woftnw.dreamvisitor.pb.PocketBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PocketBase implementation of the ServerCommandsRepository interface
 */
public class PocketBaseServerCommandsRepository implements ServerCommandsRepository {
    private static final Logger LOGGER = Logger.getLogger(PocketBaseServerCommandsRepository.class.getName());
    private static final String COLLECTION_NAME = "server_commands";
    private final PocketBase pocketBase;
    private final Gson gson;

    /**
     * Constructor for PocketBaseServerBaseRepository
     *
     * @param pocketBase The PocketBase client to use
     */
    public PocketBaseServerCommandsRepository(PocketBase pocketBase) {
        this.pocketBase = pocketBase;
        this.gson = new Gson();
    }

    /**
     * Convert a JsonObject from PocketBase to an ServerCommand object
     *
     * @param json JsonObject from PocketBase API
     * @return Mapped ServerCommand object
     */
    @NotNull
    private ServerCommand mapToServerCommand(JsonObject json) {
        ServerCommand command = new ServerCommand();

        command.setId(getStringOrNull(json, "id"));
        command.setCollectionId(getStringOrNull(json, "collectionId"));
        command.setCollectionName(getStringOrNull(json, "collectionName"));

        command.setCommand(getStringOrNull(json, "command"));
        try {
            command.setStatus(ServerCommand.Status.valueOf(getStringOrNull(json, "status")));
        } catch (IllegalArgumentException e) {
            command.setStatus(null);
        }

        command.setCreated(getOffsetDateTimeOrNull(json, "created"));
        command.setUpdated(getOffsetDateTimeOrNull(json, "updated"));

        return command;
    }

    /**
     * Convert an ServerCommand object to a JsonObject for PocketBase
     *
     * @param command ServerCommand object to convert
     * @return JsonObject for PocketBase API
     */
    @NotNull
    private JsonObject mapToJsonObject(@NotNull ServerCommand command) {
        JsonObject json = new JsonObject();

        // This is the only field for this type
        if (command.getCommand() != null)
            json.addProperty("command", command.getCommand());
        if (command.getStatus() != null)
            json.addProperty("status", command.getStatus().toString().toLowerCase());

        return json;
    }

    @Override
    public Optional<ServerCommand> findById(String id) {
        try {
            JsonObject record = pocketBase.getRecord(COLLECTION_NAME, id, null, null);
            return Optional.of(mapToServerCommand(record));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error finding server command by ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<ServerCommand> getByStatus(@NotNull ServerCommand.Status status) {
        try {
            String filter = "status = '" + status.toString().toLowerCase() + "'";
            List<JsonObject> records = pocketBase.getFullList(COLLECTION_NAME, null, null, filter, null, null);
            List<ServerCommand> commands = new ArrayList<>();
            for (JsonObject record : records) {
                commands.add(mapToServerCommand(record));
            }
            return commands;
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "No commands found with status: " + status);
            return new ArrayList<>();
        }
    }

    @Override
    public ServerCommand update(ServerCommand command) {
        try {
            JsonObject itemData = mapToJsonObject(command);

            if (command.getId() != null && !command.getId().isEmpty()) {
                // Update existing log
                JsonObject updatedRecord = pocketBase.updateRecord(COLLECTION_NAME, command.getId(), itemData, null, null);
                return mapToServerCommand(updatedRecord);
            } else {
                throw new UnsupportedOperationException("Cannot create a new command.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving log: ", e);
            throw new RuntimeException("Failed to save log", e);
        }
    }
}
