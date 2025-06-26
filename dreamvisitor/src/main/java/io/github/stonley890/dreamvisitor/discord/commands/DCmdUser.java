package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Messager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;

public class DCmdUser implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("user", "Get the details of a user.")
                .addOption(OptionType.USER, "user", "The user to search for.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Messager.debug("Command requested.");
        User targetUser = Objects.requireNonNull(event.getOption("user")).getAsUser();
        Messager.debug("Target user: " + targetUser.getId());

        // UUID from AccountLink.yml
        UUID uuid;
        uuid = AccountLink.getUuid(targetUser.getIdLong());
        String stringUuid = "N/A";
        String username = "N/A";

        if (uuid != null) {
            username = PlayerUtility.getUsernameOfUuid(uuid);
            stringUuid = uuid.toString();
        }
        if (username == null) username = "N/A";

        // Send data
        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(Color.BLUE);
        builder.setAuthor(targetUser.getName(), targetUser.getAvatarUrl(), targetUser.getAvatarUrl());

        builder.addField("ID", targetUser.getId(), false);
        builder.addField("Minecraft Username", username, false);
        builder.addField("UUID", stringUuid, false);

        event.replyEmbeds(builder.build()).queue();
    }
}
