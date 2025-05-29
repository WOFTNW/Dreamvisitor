package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.functions.AutoRestart;
import io.github.stonley890.dreamvisitor.data.PBConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

public class DCmdAutorestart extends ListenerAdapter implements DiscordCommand {
  @Override
  public @NotNull SlashCommandData getCommandData() {
    return Commands.slash("autorestart", "Restart the server when no players are online.")
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }

  @Override
  public void onCommand(@NotNull SlashCommandInteractionEvent event) {
    interaction(event);
  }

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    if (!Objects.equals(event.getButton().getId(), "autorestart")) {
      return;
    }

    interaction(event);
  }

  private static void interaction(@NotNull IReplyCallback event) {
    Button button = Button.primary("autorestart", "Undo");

    // Reload config to ensure we have latest state before checking
    PBConfigLoader.loadConfig();

    if (AutoRestart.isAutoRestart()) {
      AutoRestart.disableAutoRestart();
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setDescription("✅ Canceled server restart.").setColor(Color.BLUE).setTimestamp(Instant.now());
      event.replyEmbeds(embedBuilder.build()).addActionRow(button).queue();
    } else {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setDescription("✅ The server will restart when there are no players online.").setColor(Color.GREEN)
          .setTimestamp(Instant.now());
      event.replyEmbeds(embedBuilder.build()).addActionRow(button)
          .queue(hook -> hook.retrieveOriginal().queue(AutoRestart::enableAutoRestart));
    }
  }
}
