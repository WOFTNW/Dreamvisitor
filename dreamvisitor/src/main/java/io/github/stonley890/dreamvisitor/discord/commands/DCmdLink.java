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

public class DCmdLink implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("link", "Link a Discord account to a Minecraft account.")
                .addOption(OptionType.USER, "user", "The Discord user to register.", true)
                .addOption(OptionType.STRING, "username", "The Minecraft account to connect", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Messager.debug("Command requested.");
        User targetUser = Objects.requireNonNull(event.getOption("user")).getAsUser();
        Messager.debug("Got user.");
        String username = Objects.requireNonNull(event.getOption("username")).getAsString();
        Messager.debug("Got username.");

        UUID uuid = PlayerUtility.getUUIDOfUsername(username);
        Messager.debug("Command requested.");

        if (uuid == null) {
            event.reply("`" + username + "` could not be found!").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription(targetUser.getAsMention() + " is now linked to `" + username + "`!");
        embed.setColor(Color.GREEN);

        AccountLink.linkAccounts(uuid, targetUser.getIdLong());
        event.replyEmbeds(embed.build()).queue();
    }
}
