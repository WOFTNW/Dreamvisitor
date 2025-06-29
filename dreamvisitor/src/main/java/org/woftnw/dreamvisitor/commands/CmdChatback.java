package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.LongArgument;
import org.woftnw.dreamvisitor.functions.Chatback;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class CmdChatback implements DVCommand {
    @NotNull
    @Override
    public ExecutableCommand<?, ?> getCommand() {
        return new CommandAPICommand("chatback")
                .withOptionalArguments(new LongArgument("messageID"))
                .executesPlayer((sender, args) -> {
                    Long messageID = (Long) args.get(0);
                    if (messageID == null) {
                        sender.sendMessage(ChatColor.GRAY + "Canceled chatback.");
                        Chatback.nextChatback.remove(sender);
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Your next message will be a reply. Run /chatback to cancel.");
                        // TODO: Implement chatback.
//                        Bot.getGameChatChannel().retrieveMessageById(messageID).queue(message -> {
//                            User author = message.getAuthor();
//                            Bot.getGameChatChannel().getGuild().retrieveMemberById(author.getId()).queue(member -> {
//                                Chatback.nextChatback.put(sender, new Chatback.ReplyMessage(
//                                        member.getEffectiveName(),
//                                        author.getName(),
//                                        message.getContentRaw(),
//                                        message.getIdLong()
//                                ));
//                            });
//                        });
                    }
                });
    }
}
