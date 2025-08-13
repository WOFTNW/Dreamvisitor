package org.woftnw.dreamvisitor;

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
import org.bukkit.plugin.IllegalPluginAccessException;
import org.woftnw.dreamvisitor.commands.*;
import org.woftnw.dreamvisitor.data.*;
import org.woftnw.dreamvisitor.data.repository.*;
import org.woftnw.dreamvisitor.data.type.ServerCommand;
import org.woftnw.dreamvisitor.functions.*;
import org.woftnw.dreamvisitor.functions.worldguard.DragonFlightFlag;
import org.woftnw.dreamvisitor.functions.worldguard.WitherFlag;
import org.woftnw.dreamvisitor.listeners.*;
import org.woftnw.dreamvisitor.pb.PocketBase;
import org.woftnw.dreamvisitor.util.ConfigKey;
import net.luckperms.api.LuckPerms;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings({"null"})
public class Dreamvisitor extends JavaPlugin {

    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager
            .getRootLogger();
    public static Dreamvisitor PLUGIN;
    public static LuckPerms luckperms;
    public static String MOTD = null;
    public static Location hubLocation;
    public static boolean debugMode;
    private static ConsoleLogger appender;

    PocketBase pocketBase;

    RepositoryManager repositoryManager;

    public static StateFlag DRAGON_FLIGHT;
    public static StateFlag WITHER;

    public static Dreamvisitor getPlugin() {
        return PLUGIN;
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
        // Register WorldGuard flags
        try {
            // Get registry
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            // Create and register dragon-flight flag
            try {
                StateFlag flag = new StateFlag("dragon-flight", true);
                registry.register(flag);
                DRAGON_FLIGHT = flag;

            } catch (FlagConflictException e) {
                Flag<?> existing = registry.get("dragon-flight");
                if (existing instanceof StateFlag) {
                    DRAGON_FLIGHT = (StateFlag) existing;
                } else {
                    getLogger()
                            .severe("A flag with the name dragon-flight already exists! Some other plugin claimed it already :(");
                }
            }
            // Create and register dragon-flight flag
            try {
                StateFlag flag = new StateFlag("wither", true);
                registry.register(flag);
                WITHER = flag;

            } catch (FlagConflictException e) {
                Flag<?> existing = registry.get("wither");
                if (existing instanceof StateFlag) {
                    WITHER = (StateFlag) existing;
                } else {
                    getLogger().severe("A flag with the name wither already exists! Some other plugin claimed it already :(");
                }
            }
        } catch (NoClassDefFoundError e) {
            getLogger().warning("WorldGuard is not installed, so no flags will be created.");
        } catch (IllegalStateException e) {
            getLogger().warning("New WorldGuard flags cannot be registered at this time: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {

        PLUGIN = this;

        // Can't use debug messages until debug mode is enabled, so these first two are normal info messages.

        getLogger().info("Checking local config file...");
        checkConfig();

        // Create PocketBase client from config
        getLogger().info("Initializing PocketBase client...");
        String baseUrl = getConfig().getString("pocketbaseUrl", "http://127.0.0.1:8090/");
        String configId = getConfig().getString("pocketbaseConfigId", "");
        String token = getConfig().getString("pocketbaseToken", "");
        boolean useRealtime = getConfig().getBoolean("pocketbaseUseRealtime", true);

        if (baseUrl.isEmpty() || configId.isEmpty()) {
            getLogger().severe("PocketBase URL or Config ID is not defined. Check the config and restart the server. Dreamvisitor is disabling to prevent issues.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initialized PocketBase client");
        Map<String, Object> pbConfig = new HashMap<>();
        pbConfig.put("pocketbaseUrl", baseUrl);
        pbConfig.put("pocketbaseToken", token);
        pocketBase = PocketBase.fromConfig(pbConfig);

        getLogger().info("Initializing PocketBase config loader...");
        try {
            Config.init(pocketBase, baseUrl, configId, token, useRealtime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLogger().info("Configuration fetched. Starting enable.");

        debugMode = Config.get(ConfigKey.DEBUG);
        Messager.debug("Debug mode is enabled.");

        Messager.debug("Ensuring auto-restart is disabled...");
        if (Config.get(ConfigKey.AUTO_RESTART)) Config.set(ConfigKey.AUTO_RESTART, false);

        Messager.debug("Setting up repositories...");
        repositoryManager = new RepositoryManager(pocketBase);

        // Set up files. This must happen first because setting up commands require some files to exist.

        Messager.debug("Creating data folder...");
        boolean directoryCreated = getDataFolder().mkdir();
        if (!directoryCreated)
            Messager.debug("Dreamvisitor did not create a data folder. It may already exist.");
        saveDefaultConfig();

        Messager.debug("Initializing mail.yml");
        try {
            Mail.init();
        } catch (IOException e) {
            getLogger().warning("Unable to mail locations from " + PlayerTribe.file + ": " + e.getMessage());
        }

        Messager.debug("Initializing player-tribes.yml");
        try {
            PlayerTribe.setup();
        } catch (IOException e) {
            getLogger().warning("Unable to load tribes from " + PlayerTribe.file + ": " + e.getMessage());
        }

        Messager.debug("Initializing energy");
        Flight.init();

        Messager.debug("Initializing command scheduler");
        CommandScheduler.getInstance().loadConfig();

        Messager.debug("Initializing badwords.yml");
        try {
            BadWords.init();
        } catch (IOException e) {
            getLogger().warning("Unable to load bad words from " + BadWords.file + ": " + e.getMessage());
        }

        // Set up listeners and commands

        Messager.debug("Registering listeners...");
        registerListeners();

        List<DVCommand> commands = new ArrayList<>();
        commands.add(new CmdAdminRadio());
        commands.add(new CmdHub());
        commands.add(new CmdPanic());
        commands.add(new CmdRadio());
        commands.add(new CmdSethub());
        commands.add(new CmdSoftwhitelist());
        commands.add(new CmdTagRadio());
        commands.add(new CmdZoop());
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

        // CommandAPI is shaded into Dreamvisitor, so it must be loaded and enabled.
        Messager.debug("Loading the CommandAPI...");
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(!debugMode));
        Messager.debug("Enabling the CommandAPI...");
        CommandAPI.onEnable();
        Messager.debug("Registering " + commands.size() + " commands...");
        registerCommands(commands);

        // Set up misc

        Messager.debug("Initializing LuckPerms API...");
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null)
            luckperms = provider.getProvider();

        Messager.debug("Registering WorldGuard flag handlers...");
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(DragonFlightFlag.FACTORY, null);
        sessionManager.registerHandler(WitherFlag.FACTORY, null);

        Messager.debug("Setting up console logging...");
        appender = new ConsoleLogger();
        logger.addAppender(appender);

        Messager.debug("Setting up schedules...");

        // Fail old commands still sent in PocketBase
        List<ServerCommand> oldCommands = getRepositoryManager().getServerCommandsRepository().getByStatus(ServerCommand.Status.SENT);
        for (ServerCommand command : oldCommands) {
            command.setStatus(ServerCommand.Status.FAILED);
            getRepositoryManager().getServerCommandsRepository().update(command);
        }

        final CommandRunner commandRunner = new CommandRunner();

        Runnable runCommandsAsync = new BukkitRunnable() {
            @Override
            public void run() {
                commandRunner.run();
            }
        };

        Runnable scheduledRestarts = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Config.loadConfig();
                } catch (IOException e) {
                    getLogger().warning("Unable to load configuration!");
                }

                if (AutoRestart.isAutoRestart() && Bukkit.getOnlinePlayers().isEmpty()) {
                    AutoRestart.sendAutoRestartMessage();
                    getLogger().info("Restarting the server as scheduled.");
                    getServer().restart();
                }

                long maxMemory = Runtime.getRuntime().maxMemory();
                long freeMemory = Runtime.getRuntime().freeMemory();
                double freeMemoryPercent = ((double) freeMemory / maxMemory) * 100;
                if (freeMemoryPercent <= 10) {
                    AutoRestart.enableAutoRestart(null);
                    getLogger()
                            .warning("Dreamvisitor scheduled a restart because free memory usage is at or less than 10%.");
                }
            }
        };

        Runnable tick = new BukkitRunnable() {
            @Override
            public void run() {
                Moonglobe.tick();
            }
        };

        Runnable tickFlight = Flight::tick;

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, runCommandsAsync, 20, 40);

        Bukkit.getScheduler().runTaskTimer(this, tick, 0, 0);

        Bukkit.getScheduler().runTaskTimer(this, scheduledRestarts, 200, 1200);

        Bukkit.getScheduler().runTaskTimer(this, tickFlight, 0, 1);

        getLogger().log(Level.INFO, "Dreamvisitor has been enabled.");

    }

    /**
     * Checks the config.yml file and sets default values if they do not exist.
     */
    private void checkConfig() {
        if (!getConfig().contains("pocketbaseUrl")) {
            getConfig().set("pocketbaseUrl", "http://127.0.0.1:8090/");
        }
        if (!getConfig().contains("pocketbaseConfigId")) {
            getConfig().set("pocketbaseConfigId", "record_id_here");
        }
        if (!getConfig().contains("pocketbaseToken")) {
            getConfig().set("pocketbaseToken", "your_admin_token_here");
        }
        if (!getConfig().contains("pocketbaseUseRealtime")) {
            getConfig().set("pocketbaseUseRealtime", true);
        }

        saveConfig();
    }

    /**
     * Registers listeners so that they can receive events.
     */
    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ListenPlayerChat(), this);
        pluginManager.registerEvents(new ListenPlayerCmdPreprocess(), this);
        pluginManager.registerEvents(new ListenPlayerDeath(), this);
        pluginManager.registerEvents(new ListenPlayerJoin(), this);
        pluginManager.registerEvents(new ListenPlayerLogin(), this);
        pluginManager.registerEvents(new ListenPlayerQuit(), this);
        pluginManager.registerEvents(new ListenPlayerGameModeChange(), this);
        pluginManager.registerEvents(new ListenServerPing(), this);
        pluginManager.registerEvents(new Sandbox(), this);
        pluginManager.registerEvents(new ListenTimeSkip(), this);
        pluginManager.registerEvents(new ListenSignChange(), this);
        pluginManager.registerEvents(new ListenPlayerToggleFlight(), this);
        pluginManager.registerEvents(new ListenEntityToggleGlide(), this);
        pluginManager.registerEvents(new ListenPlayerChangedWorld(), this);
        pluginManager.registerEvents(new ListenPlayerRespawn(), this);
        pluginManager.registerEvents(new ListenCreatureSpawn(), this);
    }

    /**
     * Registers commands using CommandAPI.
     */
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

        // Disable CommandAPI
        CommandAPI.onDisable();

        // Shutdown the realtime updater
        RealtimeConfigUpdater.shutdown();

        // Remove any active moon globes
        for (Moonglobe moonglobe : Moonglobe.activeMoonglobes)
            moonglobe.remove(null);

        // Save and shutdown the Command Scheduler
        try {
            CommandScheduler.getInstance().saveConfig();
            CommandScheduler.getInstance().stopScheduler();
        } catch (IllegalPluginAccessException ignored) {
            // This might happen if Dreamvisitor fails to start correctly
        }

        // Unattach the server logger
        try {
            logger.removeAppender(appender);
        } catch (NullPointerException ignored) {
            // This might happen if Dreamvisitor fails to start correctly
        }

        // TODO: Send shutdown signal to PocketBase.

        getLogger().info("Dreamvisitor has been disabled.");
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}
