package org.woftnw.dreamvisitor.functions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Chatback {

    public static final Map<Player, ReplyMessage> nextChatback = new HashMap<>();

    public static class ReplyMessage {
        public String authorEffectiveName;
        public String authorUsername;
        public String contents;
        public long messageId;

        public ReplyMessage(@NotNull String authorEffectiveName, @NotNull String authorUsername, @NotNull String contents, long messageId) {
            this.authorEffectiveName = authorEffectiveName;
            this.authorUsername = authorUsername;
            this.contents = contents;
            this.messageId = messageId;
        }
    }

}
