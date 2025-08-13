package org.woftnw.dreamvisitor.functions;

import org.bukkit.entity.Entity;
import org.woftnw.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;

public class Messager {

    public static final Color PREFIX_COLOR = Color.decode("#3B3BD1");
    public static final Color MESSAGE_COLOR = Color.decode("#C9C9FB");
    public static final Color DANGER_COLOR = Color.decode("#E0505B");

    static final String PREFIX = ChatColor.of(PREFIX_COLOR) + "✧ ";

    /**
     * Send a {@link String} message to a target.
     *
     * @param target  the target to send the message to.
     * @param message the message to send.
     */
    public static void send(@NotNull CommandSender target, String message) {
        target.sendMessage(PREFIX + ChatColor.of(MESSAGE_COLOR) + message);
    }

    /**
     * Send a {@link BaseComponent} message to a target.
     *
     * @param target  the target to send the message to.
     * @param message the message to send.
     */
    public static void send(@NotNull CommandSender target, BaseComponent[] message) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(PREFIX).append(message).color(ChatColor.of(MESSAGE_COLOR));
        target.spigot().sendMessage(builder.build());
    }

    /**
     * Send a {@link String} message to a target with a color that indicates danger.
     *
     * @param target  the target to send the message to.
     * @param message the message to send.
     */
    public static void sendDanger(@NotNull CommandSender target, String message) {
        target.sendMessage(PREFIX + ChatColor.of(DANGER_COLOR) + message);
    }

    /**
     * Log a {@link String} message to the console if the debug mode is enabled.
     *
     * @param message the message to log.
     */
    public static void debug(String message) {
        if (Dreamvisitor.getPlugin().getConfig().getBoolean("debug")) {
            Dreamvisitor.getPlugin().getLogger().info("DEBUG: " + message);
        }
    }

    /**
     * Get the name of a {@link Collection} of {@link Player}s.
     *
     * @param targets the {@link Collection} of {@link Player}s.
     * @return the name of the {@link Collection} of {@link Player)s.
     */
    @NotNull
    public static String nameOrCountPlayer(@NotNull Collection<Player> targets) {
        if (targets.size() == 1) {
            return targets.iterator().next().getName();
        }
        return chooseCountForm(targets, "player ", "players");
    }

    /**
     * Get the name of a {@link Collection} of {@link Player}s.
     *
     * @param targets the {@link Collection} of {@link Player}s.
     * @return the name of the {@link Collection} of {@link Player)s.
     */
    @NotNull
    public static String nameOrCountEntity(@NotNull Collection<Entity> targets) {
        if (targets.size() == 1) {
            return targets.iterator().next().getName();
        }
        return chooseCountForm(targets, "entity ", "entities");
    }

    /**
     * Get the name of a {@link Collection} of {@link String}s.
     *
     * @param targets the {@link Collection} of {@link String}s.
     * @return the name of the {@link Collection} of {@link String)s.
     */
    @NotNull
    public static String nameOrCountString(@NotNull Collection<String> targets, String singular, String plural) {
        if (targets.size() == 1) {
            return targets.iterator().next();
        }
        return chooseCountForm(targets, singular, plural);
    }

    /**
     * Get a natural {@link String} representation of the number of a {@link Collection}. Sizes between 0 and 20 are
     * represented by name. All others are represented by number. If the size is one, the singular form is used.
     *
     * @param collection the {@link Collection to be counted}.
     * @param singular   the singular form.
     * @param plural     the plural form.
     * @return the {@link String} representation.
     */
    @NotNull
    public static String chooseCountForm(@NotNull Collection<?> collection, String singular, String plural) {
        int size = collection.size();
        if (size == 1) {
            return "one " + singular;
        }
        if (size < 20) {
            if (size == 0) return "zero " + plural;
            if (size == 2) return "two " + plural;
            if (size == 3) return "three " + plural;
            if (size == 4) return "four " + plural;
            if (size == 5) return "five " + plural;
            if (size == 6) return "six " + plural;
            if (size == 7) return "seven " + plural;
            if (size == 8) return "eight " + plural;
            if (size == 9) return "nine " + plural;
            if (size == 10) return "ten " + plural;
            if (size == 11) return "eleven  " + plural;
            if (size == 12) return "twelve  " + plural;
            if (size == 13) return "thirteen  " + plural;
            if (size == 14) return "fourteen  " + plural;
            if (size == 15) return "fifteen   " + plural;
            if (size == 16) return "sixteen   " + plural;
            if (size == 17) return "seventeen   " + plural;
            if (size == 18) return "eighteen   " + plural;
            return "nineteen    " + plural;
        }
        return size + " " + plural;
    }
}
