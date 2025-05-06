package io.github.stonley890.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Mail;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerCmdPreprocess implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    final String[] msgAliases = {"/msg ","/tell ","/whisper ","/reply ","/t ","/w ","/r ", "/mail send "};
    final String[] tpAliases = {
            "/call","/ecall","/tpa","/etpa","/tpask","/etpask",
            "/tpaccept","/etpaccept","/tpyes","/etpyes",
            "/home", "/ehome", "/homes", "/ehomes", "/claimspawn"
    };

    @EventHandler
    public void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event) {

        String cmd = event.getMessage();
        Player player = event.getPlayer();

        // Don't allow /tw facts reset because it is very destructive.
        if (
                cmd.stripTrailing().equalsIgnoreCase("/tw facts reset") ||
                        cmd.stripTrailing().equalsIgnoreCase("/typewriter facts reset")
        ) {
            player.sendMessage(ChatColor.RED + "Dreamvisitor stopped you from running that command because it's too destructive <3");
            event.setCancelled(true);
            return;
        }

        // '/me' and '/rp' pass through
        if ((cmd.startsWith("/me " ) || cmd.startsWith("/rp" )) && !event.isCancelled()) {

            // IF chatPaused stop /me unless bypassing
            if (Dreamvisitor.chatPaused) {

                // Init bypassed players file
                File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                List<String> bypassedPlayers;

                // Load file
                try {
                    fileConfig.load(file);
                } catch (IOException | InvalidConfigurationException e1) {
                    Bukkit.getLogger().warning("Could not load 'pauseBypass.yml' file! " + e1.getMessage());
                }

                // Remember bypassed players
                bypassedPlayers = fileConfig.getStringList("players");

                // If list contains player, allow
                if (bypassedPlayers.contains(player.getUniqueId().toString()) || player.isOp()) {
                    // Remove command
                    int spaceIndex = cmd.indexOf(' ');
                    if (spaceIndex == -1) return;
                    String action = cmd.substring(spaceIndex + 1);
                    String message = "**[" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getDisplayName())) + " **(" + player.getName()
                            + ")**]** " + ChatColor.stripColor(action);
                    // Send message
                    try {
                        Bot.getGameChatChannel().sendMessage(message).queue();
                    } catch (InsufficientPermissionException e) {
                        Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
                    }
                    Bot.sendLog(message);
                } // If list does not contain player, stop the command
                else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Chat is currently paused.");
                }
            } // If chat is not paused, allow
            else {

                // Remove command
                int spaceIndex = cmd.indexOf(' ');
                if (spaceIndex == -1) return;
                String action = cmd.substring(spaceIndex + 1);
                String message = "**[" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getDisplayName())) + " **(" + player.getName()
                        + ")**]** " + ChatColor.stripColor(action);
                // Send message
                try {
                    Bot.getGameChatChannel().sendMessage(message).queue();
                } catch (InsufficientPermissionException e) {
                    Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
                }
                Bot.sendLog(message);
            }
        } else {
            for (String string : msgAliases) {
                if (cmd.startsWith(string)) {
                    String message = "**" + Bot.escapeMarkdownFormatting(player.getName()) + "** sent command: `" + cmd + "`";
                    Bot.sendLog(message);
                    return;
                }
            }
            for (String tpAlias : tpAliases) {
                if (cmd.startsWith(tpAlias)) {
                    if (Mail.isPLayerDeliverer(player)) Mail.cancel(player);
                    for (Player sandboxer : Bukkit.getOnlinePlayers()) {
                        if (PlayerUtility.getPlayerMemory(sandboxer.getUniqueId()).sandbox && cmd.contains(sandboxer.getName())) {
                            player.sendMessage(Dreamvisitor.PREFIX + "That player is currently in Sandbox Mode. Teleportation is not allowed.");
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}