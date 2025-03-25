package io.github.stonley890.dreamvisitor;

import io.github.stonley890.dreamvisitor.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.discord.DiscEventListener;
import io.github.stonley890.dreamvisitor.discord.commands.DCmdAutorestart;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.github.stonley890.dreamvisitor.Dreamvisitor.PLUGIN;

public class Bot {

    public static final String[] TRIBE_NAMES = {"HiveWing", "IceWing", "LeafWing", "MudWing", "NightWing", "RainWing", "SandWing", "SeaWing", "SilkWing", "SkyWing"};
    private static TextChannel gameChatChannel;
    private static TextChannel gameLogChannel;
    private static TextChannel whitelistChannel;
    public static final List<Role> tribeRole = new ArrayList<>();
    static JDA jda;

    private Bot() {
        throw new IllegalStateException("Utility class.");
    }

    public static void startBot(@NotNull FileConfiguration config) {

        // Build JDA
        String token = Dreamvisitor.getPlugin().getConfig().getString("bot-token");
        // Try to create a bot
        Dreamvisitor.debug("Attempting to create a bot...");
        try {
            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .build();
            Dreamvisitor.debug("Bot created.");
            Dreamvisitor.botFailed = false;

        } catch (InvalidTokenException e) {

            Bukkit.getLogger().severe(
                    "BOT LOGIN FAILED: You need a valid bot token in dreamvisitor/config.yml. Dreamvisitor will not work properly unless there is a valid bot token.");
            Dreamvisitor.botFailed = true;

        } catch (ErrorResponseException exception) {

            if (exception.getErrorCode() == -1) {
                Bukkit.getLogger().severe("BOT LOGIN FAILED: Dreamvisitor is unable to connect to the Discord server. Dreamvisitor functionality will not work properly.");
                Dreamvisitor.botFailed = true;
            }

        }

        if (!Dreamvisitor.botFailed) {
            jda.addEventListener(new DiscCommandsManager());
            jda.addEventListener(new DiscEventListener());
            jda.addEventListener(new DCmdAutorestart());

            // Wait for bot ready
            try {
                jda.awaitReady();
                Dreamvisitor.debug("Bot is ready.");

                long chatChannelID = config.getLong("chatChannelID");
                long logChannelID = config.getLong("logChannelID");
                long whitelistChannelID = config.getLong("whitelistChannelID");

                Dreamvisitor.debug(String.valueOf(chatChannelID));
                Dreamvisitor.debug(String.valueOf(logChannelID));
                Dreamvisitor.debug(String.valueOf(whitelistChannelID));

                Bot.gameChatChannel = jda.getTextChannelById(chatChannelID);
                Bot.gameLogChannel = jda.getTextChannelById(logChannelID);
                Bot.whitelistChannel = jda.getTextChannelById(whitelistChannelID);

                if (Bot.gameChatChannel == null) Bukkit.getLogger().warning("The game chat channel with ID " + chatChannelID + " does not exist!");
                if (Bot.gameLogChannel == null) Bukkit.getLogger().warning("The game log channel with ID " + logChannelID + " does not exist!");
                if (Bot.whitelistChannel == null) Bukkit.getLogger().warning("The whitelist channel with ID " + whitelistChannelID + " does not exist!");

                for (int i = 0; i < 10; i++) Bot.tribeRole.add(jda.getRoleById(config.getLongList("tribeRoles").get(i)));

            } catch (InterruptedException exception) {
                throw new RuntimeException();
            }
        }
    }

    public static TextChannel getGameChatChannel() {
        if (gameChatChannel == null) {
            long channelId = Dreamvisitor.getPlugin().getConfig().getLong("chatChannelID");
            gameChatChannel = jda.getTextChannelById(channelId);
        }
        return gameChatChannel;
    }

    public static void setGameChatChannel(TextChannel channel) {
        gameChatChannel = channel;
        Dreamvisitor.getPlugin().getConfig().set("chatChannelID", gameChatChannel.getIdLong());
        Dreamvisitor.getPlugin().saveConfig();
    }

    public static TextChannel getWhitelistChannel() {
        if (whitelistChannel == null) {
            long channelId = Dreamvisitor.getPlugin().getConfig().getLong("whitelistChannelID");
            whitelistChannel = jda.getTextChannelById(channelId);
        }
        return whitelistChannel;
    }

    public static void setWhitelistChannel(TextChannel channel) {
        whitelistChannel = channel;
        Dreamvisitor.getPlugin().getConfig().set("whitelistChannelID", whitelistChannel.getIdLong());
        Dreamvisitor.getPlugin().saveConfig();
    }

    public static TextChannel getGameLogChannel() {
        if (gameLogChannel == null) {
            long channelId = Dreamvisitor.getPlugin().getConfig().getLong("logChannelID");
            gameLogChannel = jda.getTextChannelById(channelId);
        }
        return gameLogChannel;
    }

    public static void setGameLogChannel(TextChannel channel) {
        gameLogChannel = channel;
        Dreamvisitor.getPlugin().getConfig().set("logChannelID", gameLogChannel.getIdLong());
        Dreamvisitor.getPlugin().saveConfig();
    }

    public static JDA getJda() {
        return jda;
    }

    public static void sendLog(@NotNull String message) {
        try {
            if (!Dreamvisitor.botFailed &&!PLUGIN.getConfig().getBoolean("log-console")) Bot.getGameLogChannel().sendMessage(message).queue();
            else gameLogChannel.sendMessage(message).queue();
        } catch (InsufficientPermissionException e) {
            Bukkit.getLogger().warning("Dreamvisitor bot does not have sufficient permissions to send messages in game log channel!");
        } catch (IllegalArgumentException e) {
            if (Dreamvisitor.debugMode) Dreamvisitor.debug("Attempted to send an invalid message.");
        }
    }


    /**
     * Escapes all Discord markdown elements in a {@link String}.
     * @param string the {@link String} to format.
     * @return the formatted {@link String}.
     */
    public static @NotNull String escapeMarkdownFormatting(@NotNull String string) {
        return string.isEmpty() ? string : string.replaceAll("_","\\\\_").replaceAll("\\*","\\\\*").replaceAll("\\|","\\\\|");
    }

    @NotNull
    @Contract("_, _ -> fail")
    public static Timestamp createTimestamp(@NotNull LocalDateTime dateTime, @NotNull TimeFormat format) {
        return format.atInstant(dateTime.toInstant(OffsetDateTime.now().getOffset()));
    }
}