package io.github.stonley890.dreamvisitor.functions;


import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;

public class AutoRestart {

    private static boolean autoRestart = false;
    private static int maxPlayers;
    @Nullable
    private static Message autoRestartMessage = null;

    public static void enableAutoRestart(int maxPlayers, @Nullable Message replyMessage) {
        autoRestartMessage = replyMessage;
        AutoRestart.maxPlayers = maxPlayers;
        autoRestart = true;
    }

    public static void enableAutoRestart(@Nullable Message replyMessage) {
        autoRestartMessage = replyMessage;
        AutoRestart.maxPlayers = Dreamvisitor.getPlugin().getConfig().getInt("autoRestartMaxPlayers");;
        autoRestart = true;
    }

    public static void disableAutoRestart() {
        autoRestart = false;
        autoRestartMessage = null;
    }


    public static boolean isAutoRestart() {
        return autoRestart;
    }

    /**
     * Sends the auto restart message if necessary. This uses a blocking complete to ensure it does not get canceled.
     */
    public static void sendAutoRestartMessage() {
        if (autoRestartMessage != null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("The server is restarting in 30 seconds.").setColor(Color.GREEN).setTimestamp(Instant.now());
            autoRestartMessage.replyEmbeds(embedBuilder.build()).complete();
        }
    }

    public static int getMaxPlayers() {
        return maxPlayers;
    }
}
