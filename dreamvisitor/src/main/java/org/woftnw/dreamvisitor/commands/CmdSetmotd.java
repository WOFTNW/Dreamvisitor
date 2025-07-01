package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.functions.Messager;
import org.jetbrains.annotations.NotNull;

public class CmdSetmotd implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("setmotd")
                .withPermission("dreamvisitor.setmotd")
                .withHelp("Set or reset the MOTD.", "Set or reset the server MOTD.")
                .withOptionalArguments(new GreedyStringArgument("newMotd"))
                .executesNative((sender, args) -> {
                    final String newMotd = (String) args.get("newMotd");
                    if (newMotd == null) {
                        Dreamvisitor.MOTD = null;
                        Messager.send(sender, "Reset MOTD to default:\n" + sender.getServer().getMotd());
                        Messager.debug("Existing MOTD: " + sender.getServer().getMotd());
                    } else {
                        final String finalMotd = newMotd.replaceAll("&", "§").replaceAll("\\\\n","\n").strip();

                        Dreamvisitor.MOTD = finalMotd;
                        Messager.send(sender, "MOTD set to\n" + finalMotd);
                        Messager.debug("New MOTD: " + finalMotd);
                    }
                });
    }
}
