package io.github.stonley890.dreamvisitor.functions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Radio {
    public static void buildMessage(String message, @NotNull String name, @NotNull String command, @Nullable String tag) {

        // Set color of name to red if from console
        String finalMessage = getString(message, name, command);

        // Send messageBuilder
        Bukkit.getLogger().info(ChatColor.stripColor(finalMessage));
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

    private static @NotNull String getString(String message, @NotNull String name, @NotNull String command) {
        ChatColor nameColor = ChatColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = ChatColor.RED;
        }

        // Build messageBuilder
        String radioType = "[Staff Radio]";
        if (command.equals("aradio")) radioType = "[Admin Radio]";
        else if (command.equals("tagradio")) radioType = "[Tag Radio]";

        return ChatColor.DARK_AQUA + radioType + nameColor + " <" + name + "> " + ChatColor.WHITE + message;
    }
}
