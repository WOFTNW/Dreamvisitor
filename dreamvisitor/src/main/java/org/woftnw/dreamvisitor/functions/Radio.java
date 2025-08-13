package org.woftnw.dreamvisitor.functions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.woftnw.dreamvisitor.Dreamvisitor;

public class Radio {
    public static void buildMessage(String message, @NotNull String name, @NotNull String command, @Nullable String tag) {

        // Set color of name to red if from console
        Component finalMessage = getComponent(message, name, command);

        // Send messageBuilder
        Dreamvisitor.getPlugin().getLogger().info(PlainTextComponentSerializer.plainText().serialize(finalMessage));
        for (Player operator : Bukkit.getServer().getOnlinePlayers())
        {
            switch (command) {
                case "radio" -> {
                    if (operator.isOp() || operator.hasPermission("dreamvisitor.radio"))
                        operator.sendMessage(finalMessage);
                }
                case "aradio" -> {
                    if (operator.isOp()) operator.sendMessage(finalMessage);
                }
                case "tagradio" -> {
                    if (operator.getScoreboardTags().contains(tag) || operator.isOp()) operator.sendMessage(finalMessage);
                }
            }

        }
    }

    private static @NotNull Component getComponent(String message, @NotNull String name, @NotNull String command) {
        TextColor nameColor = NamedTextColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = NamedTextColor.RED;
        }

        // Build messageBuilder
        String radioType = "[Staff Radio]";
        if (command.equals("aradio")) radioType = "[Admin Radio]";
        else if (command.equals("tagradio")) radioType = "[Tag Radio]";

        return Component.text(radioType + nameColor + " <" + name + "> ", NamedTextColor.AQUA).append(Component.text(message, NamedTextColor.WHITE));
    }
}
