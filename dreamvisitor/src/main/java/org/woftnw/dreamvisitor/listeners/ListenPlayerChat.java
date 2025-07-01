package org.woftnw.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.entity.Player;
import org.woftnw.dreamvisitor.data.BadWords;
import org.woftnw.dreamvisitor.data.PlayerMemory;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Chatback;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import org.woftnw.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;
import org.woftnw.dreamvisitor.functions.Messager;

public class ListenPlayerChat implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerChatEvent(@NotNull AsyncPlayerChatEvent event) {

        String message = event.getMessage();
        Player player = event.getPlayer();

        // If the player has autoradio on, send there and cancel event
        if (player.hasPermission("dreamvisitor.set.autoradio")) {
            DVUser user = PlayerUtility.getUser(player);

            if (user.isAutoRadioEnabled()) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> Bukkit.dispatchCommand(player, "radio " + message));
                return;
            }
        }

        // Check for bad words

        // Get the list of bad words
        List<String> badWords = BadWords.getBadWords();

        // For each...
        for (String badWord : badWords) {

            // Create a pattern for the bad word with word boundaries
            Pattern pattern = Pattern.compile(".*\\b" + badWord.toLowerCase() + "\\b.*");

            // Check if the message matches the pattern
            if (pattern.matcher(message.toLowerCase()).matches()) {
                // Tell the player that they can't say that word
                Messager.sendDanger(player, "You aren't allowed to say " + ChatColor.YELLOW + badWord);

                // Notify social spies
                String report = "SocialSpy: " + player.getName() + " tried to say \"" + message + "\", but it was blocked because it contained the word " + badWord;
                Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                if (ess != null) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        User user = ess.getUser(onlinePlayer);
                        if (user.isSocialSpyEnabled()) {

                            Messager.send(onlinePlayer, report);
                        }
                    }
                }
                // Log the message
                Dreamvisitor.getPlugin().getLogger().info(report);

                // Stop the message from being sent
                event.setCancelled(true);
                return;
            }
        }

        // TODO: See below.
        /*
        Send chat messages to Discord
        IF chat is not paused AND the player is not an operator OR the player is an
        operator, send message
        */

        // Make sure the chat is not paused and the event is not cancelled.
        if (!Dreamvisitor.chatPaused || event.isCancelled()) {
            if (event.isCancelled()) return;

            sendMessage(player, message);

        } else {
            // The chat is paused.

            // Load pauseBypass file
            File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            List<String> bypassedPlayers;

			// Load file
            try {
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException ignored) {
            }

			// Fetch bypassed players
            bypassedPlayers = (fileConfig.getStringList("players"));

            // If player is on soft whitelist or is op, allow.
            if (bypassedPlayers.contains(player.getUniqueId().toString())
                    || player.hasPermission("dreamvisitor.nopause")) {

                sendMessage(player, message);

            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Chat is currently paused.");

                Dreamvisitor.getPlugin().getLogger().info("Message from " + player.getName() + " was blocked: " + message);

            }
        }
    }

    private static void sendMessage(Player player, String message) {
        if (Chatback.nextChatback.containsKey(player)) {
            Chatback.ReplyMessage replyMessage = Chatback.nextChatback.get(player);

            ComponentBuilder replyNotice = new ComponentBuilder();
            replyNotice.append("↱ Reply to ").color(ChatColor.GRAY);
            TextComponent replyUser = new TextComponent(replyMessage.authorEffectiveName);
            replyUser.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(replyMessage.authorUsername)));
            replyNotice.append(replyUser);

            Bukkit.spigot().broadcast(replyNotice.create());
            // TODO: Send message as reply
//                        Bot.getGameChatChannel().sendMessage(chatMessage).setMessageReference(replyMessage.messageId).failOnInvalidReply(false).queue();

            Chatback.nextChatback.remove(player);
        } else {
            // TODO: Send message
//                        Bot.getGameChatChannel().sendMessage(chatMessage).queue();
        }
    }

}
