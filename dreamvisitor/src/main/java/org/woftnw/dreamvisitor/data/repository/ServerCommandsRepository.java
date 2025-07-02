package org.woftnw.dreamvisitor.data.repository;

import org.woftnw.dreamvisitor.data.type.ServerCommand;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Item data operations
 */
public interface ServerCommandsRepository extends Repository {
    /**
     * Find a command by its PocketBase ID
     *
     * @param id PocketBase record ID
     * @return Optional containing the command if found
     */
    Optional<ServerCommand> findById(String id);

    /**
     * Get a list of commands by their status.
     *
     * @param status the status of the commands to get.
     * @return a list of commands with the specified status.
     */
    List<ServerCommand> getByStatus(ServerCommand.Status status);

    /**
     * Update a command. This will not create one.
     *
     * @param command ServerCommand to save
     * @return Saved ServerCommand
     */
    ServerCommand update(ServerCommand command);

}
