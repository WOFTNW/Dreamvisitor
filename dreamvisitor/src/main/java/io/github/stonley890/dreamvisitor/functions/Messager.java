package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Messager {

    public static final Color PREFIX_COLOR = Color.decode("#3B3BD1");
    public static final Color MESSAGE_COLOR = Color.decode("#C9C9FB");
    public static final Color DANGER_COLOR = Color.decode("#E0505B");

    static final String PREFIX = ChatColor.of(PREFIX_COLOR) + "✧ ";

    /**
     * Send a {@link String} message to a target.
     * @param target the target to send the message to.
     * @param message the message to send.
     */
    public static void send(@NotNull CommandSender target, String message) {
        target.sendMessage(PREFIX + ChatColor.of(MESSAGE_COLOR) + message);
    }

    /**
    * Send a {@link BaseComponent} message to a target.
    * @param target the target to send the message to.
    * @param message the message to send.
    */
    public static void send(@NotNull CommandSender target, BaseComponent[] message) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(PREFIX).append(message).color(ChatColor.of(MESSAGE_COLOR));
        target.spigot().sendMessage(builder.build());
    }

    /**
    * Send a {@link String} message to a target with a color that indicates danger.
    * @param target the target to send the message to.
    * @param message the message to send.
    */
    public static void sendDanger(@NotNull CommandSender target, String message) {
        target.sendMessage(PREFIX + ChatColor.of(DANGER_COLOR) + message);
    }

    /**
     * Log a {@link String} message to the console if the debug mode is enabled.
     * @param message the message to log.
     */
    public static void debug(String message) {
      if (Dreamvisitor.getPlugin().getConfig().getBoolean("debug")) {
          Dreamvisitor.getPlugin().getLogger().info("DEBUG: " + message);
      }
    }
}
