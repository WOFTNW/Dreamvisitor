package io.github.stonley890.dreamvisitor;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.data.*;
import io.github.stonley890.dreamvisitor.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.commands.CmdChatback;
import io.github.stonley890.dreamvisitor.functions.*;
import io.github.stonley890.dreamvisitor.functions.worldguard.DragonFlightFlag;
import io.github.stonley890.dreamvisitor.functions.worldguard.WitherFlag;
import io.github.stonley890.dreamvisitor.listeners.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.luckperms.api.LuckPerms;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/*
 * The main ticking thread.
 */

@SuppressWarnings({"null"})
public class Dreamvisitor extends JavaPlugin {

    public static final String PREFIX = ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "DV" + ChatColor.DARK_BLUE + "] "
            + ChatColor.RESET;
    // private
    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager
            .getRootLogger();
    // public
    public static Dreamvisitor PLUGIN;
    public static LuckPerms luckperms;
    public static String MOTD = null;
    public static boolean chatPaused;
    public static int playerLimit;
    public static Location hubLocation;
    public static boolean webWhitelistEnabled;
    public static boolean debugMode;
    public static boolean botFailed = true;
    private static ConsoleLogger appender;
    public final String VERSION = getDescription().getVersion();

    // WorldGuard flags
    public static StateFlag DRAGON_FLIGHT;
    public static StateFlag WITHER;

    public static Dreamvisitor getPlugin() {
        return PLUGIN;
    }

    public static @NotNull String getPlayerPath(@NotNull UUID uuid) {
        return PLUGIN.getDataFolder().getAbsolutePath() + "/player/" + uuid + ".yml";
    }

    public static void debug(String message) {
        if (getPlugin().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().info("DEBUG: " + message);
        }
    }

    @NotNull
    public static LuckPerms getLuckPerms() throws NullPointerException {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        LuckPerms luckPerms;
        if (provider != null)
            luckPerms = provider.getProvider();
        else {
            throw new NullPointerException("LuckPerms cannot be found.");
        }
        return luckPerms;
    }

    @Override
    public void onLoad() {
        // Called after a plugin is loaded but before it has been enabled.
        // When multiple plugins are loaded, the onLoad() for all plugins is called
        // before any onEnable() is called.
        // Init WorldGuard flags
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            try {
                // create a flag with the name "dragon-flight", defaulting to true
                StateFlag flag = new StateFlag("dragon-flight", true);
                registry.register(flag);
                DRAGON_FLIGHT = flag; // only set our field if there was no error

            } catch (FlagConflictException e) {
                // some other plugin registered a flag by the same name already.
                // you can use the existing flag, but this may cause conflicts - be sure to
                // check type
                Flag<?> existing = registry.get("dragon-flight");
                if (existing instanceof StateFlag) {
                    DRAGON_FLIGHT = (StateFlag) existing;
                } else {
                    // types don't match - this is bad news! some other plugin conflicts with you
                    // hopefully this never actually happens
                    getLogger()
                            .severe("A flag with the name dragon-flight already exists! Some other plugin claimed it already :(");
                }
            }
            try {
                // create a flag with the name "wither", defaulting to true
                StateFlag flag = new StateFlag("wither", true);
                registry.register(flag);
                WITHER = flag; // only set our field if there was no error

            } catch (FlagConflictException e) {
                // some other plugin registered a flag by the same name already.
                // you can use the existing flag, but this may cause conflicts - be sure to
                // check type
                Flag<?> existing = registry.get("wither");
                if (existing instanceof StateFlag) {
                    WITHER = (StateFlag) existing;
                } else {
                    // types don't match - this is bad news! some other plugin conflicts with you
                    // hopefully this never actually happens
                    getLogger().severe("A flag with the name wither already exists! Some other plugin claimed it already :(");
                }
            }
        } catch (NoClassDefFoundError e) {
            getLogger().info("WorldGuard is not installed, so no flags will be created.");
        } catch (IllegalStateException e) {
            getLogger().severe("New WorldGuard flags cannot be registered at this time. This should be impossible.");
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {

        try {
            // Initialize variables
            PLUGIN = this;

            debugMode = getConfig().getBoolean("debug");

            checkConfig();

            debug("Registering listeners...");
            registerListeners();

            List<DVCommand> commands = new ArrayList<>();
            commands.add(new CmdAdminRadio());
            commands.add(new CmdDiscord());
            commands.add(new CmdHub());
            commands.add(new CmdPanic());
            commands.add(new CmdPauseBypass());
            commands.add(new CmdPausechat());
            commands.add(new CmdPlayerlimit());
            commands.add(new CmdRadio());
            commands.add(new CmdSethub());
            commands.add(new CmdSoftwhitelist());
            commands.add(new CmdTagRadio());
            commands.add(new CmdZoop());
            commands.add(new CmdItemBanList());
            commands.add(new CmdUser());
            commands.add(new CmdTribeUpdate());
            commands.add(new CmdUnwax());
            commands.add(new CmdInvSwap());
            commands.add(new CmdDvset());
            commands.add(new CmdSetmotd());
            commands.add(new CmdSynctime());
            commands.add(new CmdSandbox());
            commands.add(new CmdMoonglobe());
            commands.add(new CmdSetback());
            commands.add(new CmdParcel());
            commands.add(new CmdDreamvisitor());
            commands.add(new CmdChatback());
            commands.add(new CmdVelocity());
            commands.add(new CmdSchedule());
            commands.add(new CmdInvTemplate());

            debug("Initializing commands...");
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(!debugMode));
            CommandAPI.onEnable();
            registerCommands(commands);

            debug("Creating data folder...");
            // Create config if needed
            boolean directoryCreated = getDataFolder().mkdir();
            if (!directoryCreated)
                debug("Dreamvisitor did not create a data folder. It may already exist.");
            saveDefaultConfig();

            // Initialize account link
            debug("Initializing accountLink.txt");
            AccountLink.init();

            // Initialize infractions
            debug("Initializing infractions.yml");
            Infraction.init();

            // Init alts
            debug("Initializing alts.yml");
            AltFamily.init();

            // Init eco
            debug("Initializing economy.yml");
            Economy.init();

            // Init mail
            debug("Initializing mail.yml");
            Mail.init();

            // Init tribes
            debug("Initializing player-tribes.yml");
            PlayerTribe.setup();

            // Init energy
            debug("Initializing energy");
            Flight.init();

            // Init command scheduler
            debug("Initializing command scheduler");
            CommandScheduler.getInstance().loadConfig();

            // Init bad words
            debug("Initializing badwords.yml");
            BadWords.init();

            // Init inventory templates
            debug("Initializing invTemplates.yml");
            InvTemplates.init();

            // LuckPerms API
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null)
                luckperms = provider.getProvider();

            // WorldGuard flags
            SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
            // second param allows for ordering of handlers - see the JavaDocs
            sessionManager.registerHandler(DragonFlightFlag.FACTORY, null);
            sessionManager.registerHandler(WitherFlag.FACTORY, null);

            // Start message
            getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");

            // Bot
            debug("Starting Dreamvisitor bot...");
            Bot.startBot(getConfig());

            if (!botFailed) {
                // Get saved data
                debug("Fetching recorded channels and roles from config.");
                DiscCommandsManager.init();

                // Send server start message
                try {
                    Bot.getGameLogChannel().sendMessage("Server has been started.\n*Dreamvisitor " + VERSION + "*").queue();
                } catch (InsufficientPermissionException e) {
                    Bukkit.getLogger()
                            .severe("Dreamvisitor Bot does not have permission to send messages in the game log channel!");
                    throw e;
                }

            }

            // If chat was previously paused, restore and notify in console\
            debug("Restoring chat pause...");
            if (getConfig().getBoolean("chatPaused")) {
                chatPaused = true;
                Bukkit.getServer().getLogger().info(PREFIX +
                        "Chat is currently paused from last session! Use /pausechat to allow users to chat.");
            }

            // Restore player limit override
            debug("Restoring player limit override...");
            playerLimit = getConfig().getInt("playerlimit");
            getServer().getLogger().info(PREFIX +
                    "Player limit override is currently set to " + playerLimit);

            // Create item banlist if empty
            debug("Restoring item banlist...");
            if (PLUGIN.getConfig().get("itemBlacklist") != null) {
                ArrayList<ItemStack> itemList = (ArrayList<ItemStack>) PLUGIN.getConfig().getList("itemBlacklist");
                if (itemList != null) {
                    debug("Item banlist is null. Creating an empty banlist...");
                    ItemBanList.badItems = itemList.toArray(new ItemStack[0]);
                }
            }

            // Console logging
            debug("Setting up console logging...");
            appender = new ConsoleLogger();
            logger.addAppender(appender);

            // Set up web whitelist if enabled
            webWhitelistEnabled = getConfig().getBoolean("web-whitelist");
            if (webWhitelistEnabled)
                Whitelist.startWeb(getConfig().getInt("whitelistPort"));

            Runnable pushConsole = new BukkitRunnable() {
                // Push console log to Discord every 2 seconds
                @Override
                public void run() {
                    if (Dreamvisitor.getPlugin().getConfig().getBoolean("log-console")) {

                        // If there are no messages in the queue, return
                        if (ConsoleLogger.messageBuilder.isEmpty())
                            return;

                        try {
                            Bot.getGameLogChannel().sendMessage(ConsoleLogger.messageBuilder.toString()).queue(); // send message
                        } catch (InsufficientPermissionException e) {
                            Bukkit.getLogger().warning(
                                    "Dreamvisitor Bot does not have the necessary permissions to send messages in game log channel.");
                        } catch (IllegalArgumentException e) {
                            Bukkit.getLogger().severe("Console logger tried to send an invalid message!");
                        }

                        ConsoleLogger.messageBuilder.delete(0, ConsoleLogger.messageBuilder.length()); // delete queued messages

                        // If there are no overflow messages, return
                        if (ConsoleLogger.overFlowMessages.isEmpty())
                            return;

                        StringBuilder overFlowMessageBuilder = new StringBuilder();
                        // First is safe, so add now
                        overFlowMessageBuilder.append(ConsoleLogger.overFlowMessages.get(0));

                        // For each message in overflow
                        for (int i = 1; i < ConsoleLogger.overFlowMessages.size(); i++) {

                            // Check that it fits
                            if ((overFlowMessageBuilder.toString().length() + ConsoleLogger.overFlowMessages.get(i).length()
                                    + "\n".length()) >= 2000) {
                                // if not, queue current message and clear string builder
                                try {
                                    Bot.getGameLogChannel().sendMessage(overFlowMessageBuilder.toString()).queue();
                                } catch (InsufficientPermissionException e) {
                                    Bukkit.getLogger().warning(
                                            "Dreamvisitor Bot does not have the necessary permissions to send messages in game log channel.");
                                } catch (IllegalArgumentException e) {
                                    Bukkit.getLogger().severe("Console logger tried to send an invalid message!");
                                }
                                overFlowMessageBuilder = new StringBuilder();

                            } else
                                overFlowMessageBuilder.append(ConsoleLogger.overFlowMessages.get(i)).append("\n");
                        }

                        ConsoleLogger.overFlowMessages.clear();

                    }
                }
            };

            Runnable autoRestarts = new BukkitRunnable() {
                @Override
                public void run() {
                    // Restart if requested and no players are online
                    if (AutoRestart.isAutoRestart() && Bukkit.getOnlinePlayers().size() <= AutoRestart.getMaxPlayers()) {
                        AutoRestart.sendAutoRestartMessage();
                        Bukkit.getLogger().info(PREFIX + "Restarting the server as scheduled.");
                        Bot.sendLog("**Restarting the server as scheduled.**");
                        getServer().broadcastMessage("\nThe server is performing an automatic restart in 30 seconds.\n");
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setDescription("The server is performing an automatic restart in 30 seconds.");
                        Bot.getGameChatChannel().sendMessageEmbeds(embed.build()).queue();

                        MOTD = net.md_5.bungee.api.ChatColor.RED + "The server is restarting. Please wait.";
                        getServer().setMaxPlayers(0);

                        // Schedule task
                        Bukkit.getScheduler().runTaskLater(Dreamvisitor.getPlugin(), () -> getServer().spigot().restart(), 30 * 20);
                    }

                    // also check if memory usage is high and schedule restart
                    long maxMemory = Runtime.getRuntime().maxMemory();
                    long freeMemory = Runtime.getRuntime().freeMemory();
                    double freeMemoryPercent = ((double) freeMemory / maxMemory) * 100;
                    if (freeMemoryPercent <= 10) {
                        AutoRestart.enableAutoRestart(null);
                        Bukkit.getLogger()
                                .info("Dreamvisitor enabled automatic restart because free memory usage is at or less than 10%.");
                    }
                }
            };

            Runnable tick = new BukkitRunnable() {
                @Override
                public void run() {
                    Moonglobe.tick();
//                    Flight.tick();
                }
            };

            Runnable remindWarns = new BukkitRunnable() {
                @Override
                public void run() {
                    Dreamvisitor.debug("Checking warns to be reminded.");
                    Map<Long, List<Infraction>> infractions = Infraction.getAllInfractions();
                    Dreamvisitor.debug("Got list of " + infractions.keySet().size() + " members.");
                    for (Long l : infractions.keySet()) {
                        Dreamvisitor.debug("Checking infractions of user " + l);
                        List<Infraction> userInfractions = Infraction.getInfractions(l);
                        for (Infraction userInfraction : userInfractions) {
                            Dreamvisitor.debug("Attempting remind...");
                            userInfraction.remind(l);
                        }
                    }
                }
            };

            Runnable checkBannedItems = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isOp() && ItemBanList.badItems != null) {

                            for (ItemStack item : ItemBanList.badItems) {
                                if (item == null)
                                    continue;
                                for (ItemStack content : player.getInventory().getContents()) {
                                    if (content == null || !content.isSimilar(item))
                                        continue;
                                    player.getInventory().remove(item);
                                    Bot.sendLog("Removed " + item.getType().name() + " ("
                                            + Objects.requireNonNull(item.getItemMeta()).getDisplayName() + ") from " + player.getName());
                                }
                            }
                        }
                    }
                }
            };

            Runnable checkInactivityTax = new BukkitRunnable() {
                @Override
                public void run() {
                    InactivityTax.tax();
                }
            };

            Bukkit.getScheduler().runTaskTimer(this, tick, 0, 0);

            // Push console every two seconds
            if (!botFailed)
                Bukkit.getScheduler().runTaskTimer(this, pushConsole, 0, 40);

            // Check for scheduled restart every minute
            Bukkit.getScheduler().runTaskTimer(this, autoRestarts, 200, 1200);

            // Check for warns that need to be reminded every hour
            Bukkit.getScheduler().runTaskTimer(this, remindWarns, 200, 20 * 60 * 60);

            // Check for banned items every ten seconds
            Bukkit.getScheduler().runTaskTimer(this, checkBannedItems, 40, 20 * 10);

            // Check inactivity tax every day
            Bukkit.getScheduler().runTaskTimer(this, checkInactivityTax, 200, 24 * 60 * 60 * 20);

            debug("Enable finished.");
        } catch (Exception e) {

            Bukkit.getLogger()
                    .severe("Dreamvisitor was unable to start :(\nPlease notify Bog with the following stack trace:");
            e.printStackTrace();

            if (!botFailed) {
                // Send startup crashes.
                StringBuilder builder = new StringBuilder();

                builder.append(e.getMessage());

                for (StackTraceElement stackTraceElement : e.getStackTrace())
                    builder.append("\n").append(stackTraceElement.toString());

                try {
                    Bot.getJda().retrieveUserById(505833634134228992L).complete().openPrivateChannel().complete()
                            .sendMessage(builder.toString()).complete();
                } catch (net.dv8tion.jda.api.exceptions.ErrorResponseException ex) {
                    if (ex.getErrorCode() == 50007) {
                        // Cannot send messages to this user
                        Bukkit.getLogger().warning("Unable to send Discord DM to user: " + ex.getMessage() +
                                ". User may have DMs disabled or is not in a shared server.");
                        // Log the error message to console instead
                        Bukkit.getLogger().severe("Error message that would have been sent: " + builder.toString());
                    } else {
                        // For other Discord API errors, re-throw
                        throw ex;
                    }
                }

            }

            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException();

        }
    }

    private void checkConfig() throws InvalidConfigurationException {
        if (getConfig().getLongList("triberoles").size() != 10)
            throw new InvalidConfigurationException("triberoles must contain exactly 10 entries.");
        if (getConfig().getInt("playerlimit") < -1)
            getConfig().set("playerlimit", -1);
        if (getConfig().getInt("infraction-expire-time-days") < 1)
            throw new InvalidConfigurationException("infraction-expire-time-days must be at least 1.");
        saveConfig();
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ListenEntityDamage(), this);
        pluginManager.registerEvents(new ListenPlayerChat(), this);
        pluginManager.registerEvents(new ListenPlayerCmdPreprocess(), this);
        pluginManager.registerEvents(new ListenPlayerDeath(), this);
        pluginManager.registerEvents(new ListenPlayerJoin(), this);
        pluginManager.registerEvents(new ListenPlayerLogin(), this);
        pluginManager.registerEvents(new ListenPlayerQuit(), this);
        pluginManager.registerEvents(new ItemBanList(), this);
        pluginManager.registerEvents(new ListenPlayerGameModeChange(), this);
        pluginManager.registerEvents(new ListenServerPing(), this);
        pluginManager.registerEvents(new Sandbox(), this);
        pluginManager.registerEvents(new ListenTimeSkip(), this);
        pluginManager.registerEvents(new ListenSignChangeEvent(), this);
        pluginManager.registerEvents(new ListenPlayerToggleFlightEvent(), this);
        pluginManager.registerEvents(new ListenEntityToggleGlideEvent(), this);
        pluginManager.registerEvents(new ListenPlayerChangedWorld(), this);
        pluginManager.registerEvents(new ListenPlayerRespawn(), this);
        pluginManager.registerEvents(new ListenCreatureSpawn(), this);
    }

    private void registerCommands(@NotNull List<DVCommand> commands) throws NullPointerException {
        for (DVCommand command : commands) {
            if (command.getCommand() instanceof CommandAPICommand apiCommand) {
                apiCommand.register(this);
            } else if (command.getCommand() instanceof CommandTree apiCommand) {
                apiCommand.register(this);
            }
        }
    }

    @Override
    public void onDisable() {

        CommandAPI.onDisable();

        if (!botFailed) {
            // Shutdown messages
            getLogger().info("Closing bot instance.");
            int requestsCanceled = Bot.getJda().cancelRequests();
            if (requestsCanceled > 0)
                getLogger().info(requestsCanceled + " queued bot requests were canceled for shutdown.");
            Bot.getGameLogChannel().sendMessage("*Server has been shut down.*").complete();
            Bot.getJda().shutdownNow();
        }

        // remove moon globes
        for (Moonglobe moonglobe : Moonglobe.activeMoonglobes)
            moonglobe.remove(null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerUtility.savePlayerMemory(player.getUniqueId());
                PlayerUtility.clearPlayerMemory(player.getUniqueId());
            } catch (IOException e) {
                Bukkit.getLogger().severe("Unable to save player memory! Does the server have write access?");
                if (Dreamvisitor.debugMode)
                    throw new RuntimeException();
            }
        }

        // Save command scheduler
        CommandScheduler.getInstance().saveConfig();
        CommandScheduler.getInstance().stopScheduler();

        logger.removeAppender(appender);
    }

}
