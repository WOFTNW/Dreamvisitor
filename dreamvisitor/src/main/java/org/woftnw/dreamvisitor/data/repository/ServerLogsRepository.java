package org.woftnw.dreamvisitor.data.repository;

import org.woftnw.dreamvisitor.data.type.ServerLog;

import java.util.List;

/**
 * Repository interface for ServerLog data operations
 */
public interface ServerLogsRepository extends Repository {
    /**
     * Get the most recent n logs
     *
     * @return List of most recent n logs
     */
    List<ServerLog> findLast(int count);

    /**
     * Save a log (create or update)
     *
     * @param item ServerLog to save
     * @return Saved ServerLog
     */
    ServerLog create(ServerLog item);

}
