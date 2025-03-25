package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.AutoRestart;
import org.jetbrains.annotations.NotNull;

public class CmdScheduleRestart implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("schedulerestart")
                .withPermission(CommandPermission.OP)
                .withHelp("Schedule a restart.", "Restart the server when no players are online.")
                .executesNative((sender, args) -> {
                    if (AutoRestart.isAutoRestart()) {
                        AutoRestart.disableAutoRestart();
                        sender.sendMessage(Dreamvisitor.PREFIX + "Canceled server restart. Run /autorestart again to cancel.");
                    } else {
                        AutoRestart.enableAutoRestart(null);
                        sender.sendMessage(Dreamvisitor.PREFIX + "The server will restart when there are no players online. Run /autorestart again to cancel.");
                    }
                });
    }
}
