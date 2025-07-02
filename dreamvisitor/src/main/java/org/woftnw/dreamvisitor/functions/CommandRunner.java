package org.woftnw.dreamvisitor.functions;

import org.bukkit.Bukkit;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.data.repository.ServerCommandsRepository;
import org.woftnw.dreamvisitor.data.type.ServerCommand;

import java.util.List;

public class CommandRunner {

    private final ServerCommandsRepository serverCommandsRepository = Dreamvisitor.getPlugin().getRepositoryManager().getServerCommandsRepository();

    public void run() {
        // Get new commands
        List<ServerCommand> commands = serverCommandsRepository.getByStatus(ServerCommand.Status.SENT);
//        Messager.debug("Got " + commands.size() + " new commands.");

        for (ServerCommand command : commands) {
            // Set the status to received
            command.setStatus(ServerCommand.Status.RECEIVED);
            // Update PocketBase
            serverCommandsRepository.update(command);

//            Messager.debug("Updated statuses. Executing commands.");

            // Schedule execution
            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
//                Messager.debug("Executing command " + command.getCommand());
                // Run the command
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.getCommand());
                // Store the result in the status
                if (success) command.setStatus(ServerCommand.Status.EXECUTED);
                else command.setStatus(ServerCommand.Status.FAILED);
//                Messager.debug("Command was " + command.getStatus());
                // Update PocketBase asynchronously
                Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> serverCommandsRepository.update(command));
            });
        }
    }

}
