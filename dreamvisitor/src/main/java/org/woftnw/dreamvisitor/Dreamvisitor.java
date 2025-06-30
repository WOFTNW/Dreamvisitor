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
import org.woftnw.dreamvisitor.commands.*;
import org.woftnw.dreamvisitor.data.*;
import org.woftnw.dreamvisitor.functions.*;
import org.woftnw.dreamvisitor.functions.worldguard.DragonFlightFlag;
import org.woftnw.dreamvisitor.functions.worldguard.WitherFlag;
import org.woftnw.dreamvisitor.listeners.*;
import org.woftnw.dreamvisitor.util.ConfigKey;
import net.luckperms.api.LuckPerms;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

@SuppressWarnings({"null"})
public class Dreamvisitor extends JavaPlugin {

    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager
            .getRootLogger();
    public static Dreamvisitor PLUGIN;
    public static LuckPerms luckperms;
    public static String MOTD = null;
    public static boolean chatPaused;
    public static int playerLimit;
    public static Location hubLocation;
    public static boolean debugMode;
    private static ConsoleLogger appender;

    public static StateFlag DRAGON_FLIGHT;
    public static StateFlag WITHER;

    public static Dreamvisitor getPlugin() {
        return PLUGIN;
    }

    public static @NotNull String getPlayerPath(@NotNull UUID uuid) {
        return PLUGIN.getDataFolder().getAbsolutePath() + "/player/" + uuid + ".yml";
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
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
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
            getLogger().info("WorldGuard is not installed, so no flags will be created.");
        } catch (IllegalStateException e) {
            getLogger().warning("New WorldGuard flags cannot be registered at this time. You may not have WorldGuard installed.");
        }
    }

    @Override
    public void onEnable() {

        PLUGIN = this;

        debugMode = Config.get(ConfigKey.DEBUG);

        checkConfig();

        Messager.debug("Initializing PocketBase config loader...");
        Config.init();

        Messager.debug("Registering listeners...");
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
        commands.add(new CmdScheduleRestart());
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

        Messager.debug("Initializing commands...");
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(!debugMode));
        CommandAPI.onEnable();
        registerCommands(commands);

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

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null)
            luckperms = provider.getProvider();

        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(DragonFlightFlag.FACTORY, null);
        sessionManager.registerHandler(WitherFlag.FACTORY, null);

        getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for Wings of Fire: The New World to add various features.");

        Messager.debug("Restoring chat pause...");
        if (Config.get(ConfigKey.PAUSE_CHAT)) {
            chatPaused = true;
            getLogger().info("Chat is currently paused from last session! Use /pausechat to allow users to chat.");
        }

        Messager.debug("Restoring player limit override...");
        playerLimit = Config.get(ConfigKey.PLAYER_LIMIT);
        getLogger().info("Player limit override is currently set to " + playerLimit);

        Messager.debug("Restoring item banlist...");
        try {
            ItemBanList.init();
        } catch (IOException e) {
            getLogger().warning("Unable to load banned items from " + ItemBanList.file + ": " + e.getMessage());
        }

        Messager.debug("Setting up console logging...");
        appender = new ConsoleLogger();
        logger.addAppender(appender);

        Runnable pushConsole = new BukkitRunnable() {
            @Override
            public void run() {
                if (Config.get(ConfigKey.LOG_CONSOLE)) {



                }
            }
        };

        Runnable scheduledRestarts = new BukkitRunnable() {
            @Override
            public void run() {
                Config.loadConfig();

                if (AutoRestart.isAutoRestart() && Bukkit.getOnlinePlayers().isEmpty()) {
                    AutoRestart.sendAutoRestartMessage();
                    getLogger().info("Restarting the server as scheduled.");
                    getServer().spigot().restart();
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
                                getLogger().info("Removed " + item.getType().name() + " ("
                                        + Objects.requireNonNull(item.getItemMeta()).getDisplayName() + ") from " + player.getName());
                            }
                        }
                    }
                }
            }
        };

        Bukkit.getScheduler().runTaskTimer(this, tick, 0, 0);

        Bukkit.getScheduler().runTaskTimer(this, scheduledRestarts, 200, 1200);

        Bukkit.getScheduler().runTaskTimer(this, checkBannedItems, 40, 20 * 10);

        Messager.debug("Enable finished.");

    }

    private void checkConfig() {
//    if (getConfig().getLongList("triberoles").size() != 10)
//      throw new InvalidConfigurationException("triberoles must contain exactly 10 entries.");
//    if (getConfig().getInt("playerlimit") < -1)
//      getConfig().set("playerlimit", -1);
//    if (getConfig().getInt("infraction-expire-time-days") < 1)
//      throw new InvalidConfigurationException("infraction-expire-time-days must be at least 1.");

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
        pluginManager.registerEvents(new ListenPlayerMoveEvent(), this);
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

        // Shutdown the realtime updater
        RealtimeConfigUpdater.shutdown();

        for (Moonglobe moonglobe : Moonglobe.activeMoonglobes)
            moonglobe.remove(null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerUtility.savePlayerMemory(player.getUniqueId());
                PlayerUtility.clearPlayerMemory(player.getUniqueId());
            } catch (IOException e) {
                getLogger().severe("Unable to save player memory! Does the server have write access?");
                if (Dreamvisitor.debugMode)
                    throw new RuntimeException();
            }
        }

        CommandScheduler.getInstance().saveConfig();
        CommandScheduler.getInstance().stopScheduler();

        logger.removeAppender(appender);
    }

}
