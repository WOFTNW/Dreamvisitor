package org.woftnw.dreamvisitor.data.repository;

import org.woftnw.dreamvisitor.data.type.ServerLog;

import java.util.List;

/**
 * Repository interface for Item data operations
 */
public interface ServerLogsRepository extends Repository {
    /**
     * Get the most recent n logs
     *
     * @return List of most recent n logs
     */
    List<ServerLog> findLast(int count);

    /**
     * Save an item (create or update)
     *
     * @param item Item to save
     * @return Saved item
     */
    ServerLog create(ServerLog item);

}
