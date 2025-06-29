package org.woftnw.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.woftnw.dreamvisitor.data.BadWords;
import org.woftnw.dreamvisitor.data.PlayerMemory;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.functions.Chatback;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
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

public class ListenPlayerChat implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerChatEvent(@NotNull AsyncPlayerChatEvent event) {

        if (event.getPlayer().hasPermission("dreamvisitor.set.autoradio")) {
            PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

            if (memory.autoRadio) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> Bukkit.dispatchCommand(event.getPlayer(), "radio " + event.getMessage()));
                return;
            }
        }

        List<String> badWords = BadWords.getBadWords();

        String message = event.getMessage();

        for (String badWord : badWords) {

            Pattern pattern = Pattern.compile(".*\\b" + badWord + "\\b.*");

            if (pattern.matcher(message).matches()) {
                event.getPlayer().sendMessage(ChatColor.RED + "You can't say " + ChatColor.YELLOW + badWord + ChatColor.RED + "!");
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


        if (!Dreamvisitor.chatPaused || event.isCancelled()) {
            if (event.isCancelled()) return;

            try {
                if (Chatback.nextChatback.containsKey(event.getPlayer())) {
                    Chatback.ReplyMessage replyMessage = Chatback.nextChatback.get(event.getPlayer());

                    ComponentBuilder replyNotice = new ComponentBuilder();
                    replyNotice.append("↱ Reply to ").color(ChatColor.GRAY);
                    TextComponent replyUser = new TextComponent();
                    replyUser.setText(replyMessage.authorEffectiveName);
                    replyUser.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(replyMessage.authorUsername)));
                    replyNotice.append(replyUser);

                    // TODO: Send message with reply
//                    Bot.getGameChatChannel().sendMessage(chatMessage).setMessageReference(replyMessage.messageId).failOnInvalidReply(false).queue();

                    Bukkit.spigot().broadcast(replyNotice.create());

                    Chatback.nextChatback.remove(event.getPlayer());
                } else {
                    // TODO: Send message
//                    Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                }

            } catch (InsufficientPermissionException e) {
                Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
            }

        } else {

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
            if (bypassedPlayers.contains(event.getPlayer().getUniqueId().toString())
                    || event.getPlayer().hasPermission("dreamvisitor.nopause")) {

                try {
                    if (Chatback.nextChatback.containsKey(event.getPlayer())) {
                        Chatback.ReplyMessage replyMessage = Chatback.nextChatback.get(event.getPlayer());

                        ComponentBuilder replyNotice = new ComponentBuilder();
                        replyNotice.append("↱ Reply to ").color(ChatColor.GRAY);
                        TextComponent replyUser = new TextComponent(replyMessage.authorEffectiveName);
                        replyUser.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(replyMessage.authorUsername)));
                        replyNotice.append(replyUser);

                        Bukkit.spigot().broadcast(replyNotice.create());
                        // TODO: Send message as reply
//                        Bot.getGameChatChannel().sendMessage(chatMessage).setMessageReference(replyMessage.messageId).failOnInvalidReply(false).queue();

                        Chatback.nextChatback.remove(event.getPlayer());
                    } else {
                        // TODO: Send message
//                        Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                    }
                } catch (InsufficientPermissionException e) {
                    Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
                }

            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Chat is currently paused.");

                Dreamvisitor.getPlugin().getLogger().info("Message from " + event.getPlayer().getName() + " was blocked: " + event.getMessage());

            }
        }
    }

}
