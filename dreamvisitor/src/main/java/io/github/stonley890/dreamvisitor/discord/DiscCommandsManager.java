package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.discord.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DiscCommandsManager extends ListenerAdapter {

    static final JDA jda = Bot.getJda();

    static final List<DiscordCommand> commands = new ArrayList<>();


    // Get channels and roles from config
    @SuppressWarnings({"null"})
    public static void init() {

        Dreamvisitor.debug("Initializing commands...");

        List<DiscordCommand> addList = new ArrayList<>();

        addList.add(new DCmdActivity());
        addList.add(new DCmdBroadcast());
        addList.add(new DCmdLink());
        addList.add(new DCmdList());
        addList.add(new DCmdMsg());
        addList.add(new DCmdPanic());
        addList.add(new DCmdResourcepackupdate());
        addList.add(new DCmdSchedulerestart());
        addList.add(new DCmdSetgamechat());
        addList.add(new DCmdSetlogchat());
        addList.add(new DCmdSetrole());
        addList.add(new DCmdSetwhitelist());
        addList.add(new DCmdToggleweb());
        addList.add(new DCmdUnwhitelist());
        addList.add(new DCmdUser());
        addList.add(new DCmdWarn());
        addList.add(new DCmdAlts());
        addList.add(new DCmdInfractions());

        Dreamvisitor.debug("Ready to add to guild.");

        addCommands(addList);

    }

    @Override
    @SuppressWarnings({"null"})
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        for (DiscordCommand command : commands) {
            if (event.getName().equals(command.getName())) {
                command.onCommand(event);
                return;
            }
        }
        event.reply("No commands match your request. This is a fatal error and should not be possible.\n" +
                "*Great, everything is broken. I'm going to have to bother one of my superiors to fix this.*").queue();
    }

    // Register commands on ready
    @Override
    @SuppressWarnings({"null"})
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        for (DiscordCommand command : commands) {
            commandData.add(command.getCommandData());
            Dreamvisitor.debug("Added command " + command.getName());
        }

        // register commands
        event.getGuild().updateCommands().addCommands(commandData).queue();

        Dreamvisitor.debug("Updated commands.");

        commandData.clear();

    }

    public static void addCommands(@NotNull List<DiscordCommand> commands) {

        Dreamvisitor.debug("Request to add " + commands.size() + " commands.");

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<SlashCommandData> commandData = new ArrayList<>();
        for (DiscordCommand command : commands) {
            commandData.add(command.getCommandData());
            Dreamvisitor.debug("Added command " + command.getName());
        }

        for (Guild guild : jda.getGuilds()) {
            // register commands
            for (SlashCommandData commandDatum : commandData) {
                guild.upsertCommand(commandDatum).queue();
            }
        }

        Dreamvisitor.debug("Updated commands for " + jda.getGuilds().size() + " guild(s).");

        DiscCommandsManager.commands.addAll(commands);

    }

}
