package io.github.stonley890.dreamvisitor.functions;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Messager {

    static final Color PREFIX_COLOR = Color.decode("#3B3BD1");
    static final Color MESSAGE_COLOR = Color.decode("#C9C9FB");

    static final String PREFIX = ChatColor.of(PREFIX_COLOR) + "✧ ";

    /**
     * Send a {@link String} message to a player.
     * @param player the player to send the message to.
     * @param message the message to send.
     */
    public static void send(@NotNull Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.of(MESSAGE_COLOR) + message);
    }

    /**
    * Send a {@link BaseComponent} message to a player.
    * @param player the player to send the message to.
    * @param message the message to send.
    */
    public static void send(@NotNull Player player, BaseComponent[] message) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(PREFIX).append(message).color(ChatColor.of(MESSAGE_COLOR));
        player.spigot().sendMessage(builder.build());
    }
}
