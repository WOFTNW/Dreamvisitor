package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.data.PBConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;

public class AutoRestart {

  private static boolean localAutoRestart = false;
  @Nullable
  private static Message autoRestartMessage = null;

  public static void enableAutoRestart(@Nullable Message replyMessage) {
    autoRestartMessage = replyMessage;
    localAutoRestart = true;
    // Update the PocketBase config
    PBConfigLoader.updateConfigField("autoRestart", true);
  }

  public static void disableAutoRestart() {
    localAutoRestart = false;
    autoRestartMessage = null;
    // Update the PocketBase config
    PBConfigLoader.updateConfigField("autoRestart", false);
  }

  public static boolean isAutoRestart() {
    // Check PocketBase config first, fall back to local state if needed
    return PBConfigLoader.getBoolean("autoRestart", localAutoRestart);
  }

  /**
   * Sends the auto restart message if necessary. This uses a blocking complete to
   * ensure it does not get canceled.
   */
  public static void sendAutoRestartMessage() {
    if (autoRestartMessage != null) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setDescription("The server is now restarting.").setColor(Color.GREEN).setTimestamp(Instant.now());
      autoRestartMessage.replyEmbeds(embedBuilder.build()).complete();
    }
  }
}
