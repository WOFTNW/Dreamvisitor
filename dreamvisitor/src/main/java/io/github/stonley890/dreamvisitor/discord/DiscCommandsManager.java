package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.discord.commands.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

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
        addList.add(new DCmdAutorestart());
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
        addList.add(new DCmdBalance());
        addList.add(new DCmdInventory());
        addList.add(new DCmdShop());
        addList.add(new DCmdEconomy());
        addList.add(new DCmdEcostats());
        addList.add(new DCmdDaily());
        addList.add(new DCmdWork());
        addList.add(new DCmdBaltop());
        addList.add(new DCmdSeen());
        addList.add(new DCmdYear());

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
        EmbedBuilder noMatchEmbed = new EmbedBuilder();
        noMatchEmbed.setColor(Color.RED).setTitle("No commands match your request.").setDescription("This is a fatal error and should not be possible. The command has been scheduled for deletion to prevent further exceptions.");
        event.reply("Great, everything is broken. I'm going to have to bother one of my superiors to fix this.").addEmbeds(noMatchEmbed.build()).queue();

        String commandId = event.getCommandId();
        event.getJDA().deleteCommandById(commandId).queue();
    }

    public static void addCommands(@NotNull List<DiscordCommand> commands) {

        Dreamvisitor.debug("Request to add " + commands.size() + " commands.");

        if (commands.isEmpty()) return;

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<SlashCommandData> commandData = new ArrayList<>();
        for (DiscordCommand command : commands) {
            if (command != null) {
                commandData.add(command.getCommandData());
                try {
                    jda.addEventListener(command);
                } catch (IllegalArgumentException ignored) {}
                Dreamvisitor.debug("Added command " + command.getName());
            }
        }

        for (Guild guild : jda.getGuilds()) {
            // register commands
            for (SlashCommandData commandDatum : commandData) {
                guild.upsertCommand(commandDatum).queue();
            }
        }

        Dreamvisitor.debug("Updated commands for " + jda.getGuilds().size() + " guild(s).");

        commands.removeIf(Objects::isNull);
        DiscCommandsManager.commands.addAll(commands);

    }

}
