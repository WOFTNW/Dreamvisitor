package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.AutoRestart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
        return Commands.slash("autorestart", "Restart the server when a certain number of players are online.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .addOption(OptionType.INTEGER, "max-players", "The maximum number of players that auto-restart with function.", false);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        if (AutoRestart.isAutoRestart()) {
            AutoRestart.disableAutoRestart();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("✅ Canceled server restart.").setColor(Color.BLUE).setTimestamp(Instant.now());
            event.replyEmbeds(embedBuilder.build()).queue();
        } else {

            Integer maxPlayers = event.getOption("max-players", OptionMapping::getAsInt);
            if (maxPlayers == null) maxPlayers = Dreamvisitor.getPlugin().getConfig().getInt("autoRestartMaxPlayers");
            else if (maxPlayers < 0) {
                event.reply("max-players cannot be negative.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("✅ The server will restart when there are at most " + maxPlayers + " players online.").setColor(Color.GREEN).setTimestamp(Instant.now());
            Integer finalMaxPlayers = maxPlayers;
            event.replyEmbeds(embedBuilder.build()).queue(hook -> {
                hook.retrieveOriginal().queue(message -> AutoRestart.enableAutoRestart(finalMaxPlayers, message));
            });
        }
    }

}
