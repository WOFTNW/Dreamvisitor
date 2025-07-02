package org.woftnw.dreamvisitor.data.repository;

import org.woftnw.dreamvisitor.pb.PocketBase;

/**
 * This class stores repositories for access by other functions. The one instance of this is created in the Dreamvisitor enable process and can be accessed with Dreamvisitor.getPlugin().getRepositoryManager()
 */
public class RepositoryManager {

    final ServerLogsRepository serverLogsRepository;
    final UserRepository userRepository;
    final ServerCommandsRepository serverCommandsRepository;

    public RepositoryManager(PocketBase pocketBase) {
        serverLogsRepository = new PocketBaseServerLogsRepository(pocketBase);
        userRepository = new PocketBaseUserRepository(pocketBase);
        serverCommandsRepository = new PocketBaseServerCommandsRepository(pocketBase);
    }

    public ServerLogsRepository getServerLogsRepository() {
        return serverLogsRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public ServerCommandsRepository getServerCommandsRepository() {
        return serverCommandsRepository;
    }
}
