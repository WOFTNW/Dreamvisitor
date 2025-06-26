package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Messager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class DCmdSeen implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("seen", "See when someone was last on the Minecraft server.")
                .addOption(OptionType.USER, "user", "The user to search for.", true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {


        User user = event.getOption("user", OptionMapping::getAsUser);
        if (user == null) {
            event.reply("user cannot be null!").setEphemeral(true).queue();
            return;
        }
        UUID uuid = AccountLink.getUuid(user.getIdLong());
        if (uuid == null) {
            event.reply(user.getAsMention() + " does not have a linked Minecraft account.").setEphemeral(true).queue();
            return;
        }
        Messager.debug("UUID: " + uuid);
        boolean online = Bukkit.getPlayer(uuid) != null;
        Messager.debug("Online? " + online);
        Instant time;
        try {
            if (online) {
                time = PlayerUtility.getLastLogin(uuid);
            } else {
                time = PlayerUtility.getLastLogout(uuid);
            }
        } catch (Exception e) {
            event.reply("EssentialsX is not currently active!").setEphemeral(true).queue();
            return;
        }

        Duration duration = Duration.between(time, Instant.ofEpochMilli(System.currentTimeMillis()));

        EmbedBuilder embed = new EmbedBuilder();
        String status = "offline";
        if (online) status = "online";
        embed.setDescription(user.getAsMention() + " has been " + status + " since " + duration.toDaysPart() + " days, " + duration.toHoursPart() + " hours, " + duration.toMinutesPart() + " minutes, and " + duration.toSecondsPart() + " seconds ago.");
        embed.setColor(Color.BLUE);
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

    }
}
