package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.woftnw.dreamvisitor.data.PlayerUtility;
import org.woftnw.dreamvisitor.data.type.DVUser;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CmdDvset implements DVCommand {

    private static void sendUserGui(@NotNull Player player) {

        DVUser user = PlayerUtility.getUser(player);

        Component message = Component.text("User Options ");

        if (player.hasPermission("dreamvisitor.set.discord")) {
            message = message.append(Component.text("\n\nDiscord Visibility: ").color(NamedTextColor.WHITE))
                    .append(Component.text("\nWhether to show messages from Discord's chat bridge.").color(NamedTextColor.GRAY))
                    .append(Component.text("\n[").color(NamedTextColor.DARK_GRAY))
                    .append(booleanToggle(user.isShowDiscordOn(), "discord"))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
        }

        if (player.hasPermission("dreamvisitor.set.flight")) {
            message = message.append(Component.text("\n\nFlight Disabled: ").color(NamedTextColor.WHITE))
                    .append(Component.text("\nWhether Flight Mode cannot be activated.").color(NamedTextColor.GRAY))
                    .append(Component.text("\n[").color(NamedTextColor.DARK_GRAY))
                    .append(booleanToggle(user.isFlightDisabled(), "flight"))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
        }

        if (player.hasPermission("dreamvisitor.set.zoop")) {
            message = message.append(Component.text("\n\nDiscord Vanish: ").color(NamedTextColor.WHITE))
                    .append(Component.text("\nWhether to appear offline in Dreamvisitor.").color(NamedTextColor.GRAY))
                    .append(Component.text("\n[").color(NamedTextColor.DARK_GRAY))
                    .append(booleanToggle(user.isVanished(), "vanished"))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
        }

        if (player.hasPermission("dreamvisitor.set.autoinvswap")) {
            message = message.append(Component.text("\n\nAutomatic Inventory Swap: ").color(NamedTextColor.WHITE))
                    .append(Component.text("\nWhether to automatically swap inventories on game mode change.").color(NamedTextColor.GRAY))
                    .append(Component.text("\n[").color(NamedTextColor.DARK_GRAY))
                    .append(booleanToggle(user.isAutoInvSwapEnabled(), "autoinvswap"))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
        }

        if (player.hasPermission("dreamvisitor.set.autoradio")) {
            message = message.append(Component.text("\n\nAutomatic Radio: ").color(NamedTextColor.WHITE))
                    .append(Component.text("\nWhether to send all messages to staff radio.").color(NamedTextColor.GRAY))
                    .append(Component.text("\n[").color(NamedTextColor.DARK_GRAY))
                    .append(booleanToggle(user.isAutoRadioEnabled(), "autoradio"))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
        }

        message = message.append(Component.text("\n"));
        Messager.send(player, message);

    }

    /**
     * Creates a {@link Component} representing a {@code boolean} value with a
     * command to change it.
     *
     * @param value   the {@code boolean} to display.
     * @param cmdName the command to run. This will be formatted as
     *                {@code /dvset <state> <cmdName> !value}
     * @return a {@link Component} representing the value with a command to
     * change it.
     */
    private static @NotNull Component booleanToggle(boolean value, String cmdName) {
        Component toggle = Component.empty();

        if (value) {
            toggle = toggle.append(
                    Component.text("-O")
                            .decorate(TextDecoration.UNDERLINED)
                            .color(NamedTextColor.GREEN)
            );
        } else {
            toggle = toggle.append(Component.text("0-").decorate(TextDecoration.UNDERLINED).color(NamedTextColor.RED));
        }

        return toggle.hoverEvent(HoverEvent.showText(
                Component.text("Currently toggled to ")
                        .append(Component.text(String.valueOf(value).toUpperCase())).color(NamedTextColor.WHITE)
                        .append(Component.text("."))
        )).clickEvent(ClickEvent.runCommand("/dvset " + cmdName + " " + String.valueOf(!value).toLowerCase()));
    }

    @NotNull
    @Override
    public CommandTree getCommand() {

        return new CommandTree("dvset")
                .withPermission(CommandPermission.fromString("dreamvisitor.userset"))
                .withHelp("Manage settings.", "Manage your Dreamvisitor settings.")
                .executesNative(((sender, args) -> {
                    if (sender instanceof Player player && sender.hasPermission("dreamvisitor.userset")) {
                        // Player GUI
                        sendUserGui(player);
                    } else
                        throw CommandAPI.failWithString("No options specified.");
                }))
                .then(new StringArgument("option")
                        .setOptional(true)
                        .executesNative((sender, args) -> {
                            String option = (String) args.get("option");

                            CommandSender callee = sender.getCallee();
                            if (!callee.hasPermission("dreamvisitor.userset"))
                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");

                            DVUser user;

                            if (callee instanceof Player player) {
                                user = PlayerUtility.getUser(player);
                            } else
                                throw CommandAPI.failWithString("This can only be executed by a player!");

                            switch (Objects.requireNonNull(option)) {
                                case "discord" -> {
                                    if (!player.hasPermission("dreamvisitor.set.discord"))
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                    Messager.send(callee, Component.text("Discord Visibility is currently set to ").color(NamedTextColor.GRAY)
                                            .append(Component.text(user.isShowDiscordOn()).color(NamedTextColor.WHITE)));
                                }
                                case "flight" -> {
                                    if (!player.hasPermission("dreamvisitor.set.flight"))
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                    Messager.send(callee, Component.text("Flight Enabled is currently set to ").color(NamedTextColor.GRAY)
                                            .append(Component.text(user.isFlightDisabled()).color(NamedTextColor.WHITE)));
                                }
                                case "vanished" -> {
                                    if (!player.hasPermission("dreamvisitor.set.zoop"))
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                    Messager.send(callee, Component.text("Discord Vanish is currently set to ").color(NamedTextColor.GRAY)
                                            .append(Component.text(user.isVanished()).color(NamedTextColor.WHITE)));
                                }
                                case "autoinvswap" -> {
                                    if (!player.hasPermission("dreamvisitor.set.autoinvswap"))
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                    Messager.send(callee, Component.text("Automatic Inventory Swap is currently set to ").color(NamedTextColor.GRAY)
                                            .append(Component.text(user.isAutoInvSwapEnabled()).color(NamedTextColor.WHITE)));
                                }
                                case "autoradio" -> {
                                    if (!player.hasPermission("dreamvisitor.set.autoradio"))
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                    Messager.send(callee, Component.text("Automatic Radio is currently set to ").color(NamedTextColor.GRAY)
                                            .append(Component.text(user.isAutoRadioEnabled()).color(NamedTextColor.WHITE)));
                                }
                                default ->
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                            }
                        })
                        .then(new StringArgument("modification")
                                .setOptional(true)
                                .executesNative(((sender, args) -> {
                                    String option = (String) args.get("option");
                                    String modification = (String) args.get("modification");

                                    CommandSender callee = sender.getCallee();
                                    if (!callee.hasPermission("dreamvisitor.userset"))
                                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");

                                    DVUser user;

                                    if (callee instanceof Player player) {
                                        user = PlayerUtility.getUser(player);
                                    } else
                                        throw CommandAPI.failWithString("This can only be executed by a player!");

                                    if (option == null || modification == null) {
                                        sendUserGui(player);
                                        return;
                                    }

                                    switch (option) {
                                        case "discord" -> {
                                            if (!player.hasPermission("dreamvisitor.set.discord"))
                                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                            user.setShowDiscordMessages(Boolean.parseBoolean(modification));
                                            Messager.send(callee, Component.text("Discord Visibility is currently set to ").color(NamedTextColor.GRAY)
                                                    .append(Component.text(user.isShowDiscordOn()).color(NamedTextColor.WHITE)));
                                        }
                                        case "flight" -> {
                                            if (!player.hasPermission("dreamvisitor.set.flight"))
                                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                            user.setFlightDisabled(Boolean.parseBoolean(modification));
                                            Messager.send(callee, Component.text("Flight Enabled is currently set to ").color(NamedTextColor.GRAY)
                                                    .append(Component.text(user.isFlightDisabled()).color(NamedTextColor.WHITE)));
                                        }
                                        case "vanished" -> {
                                            if (!player.hasPermission("dreamvisitor.set.zoop"))
                                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                            user.setVanished(Boolean.parseBoolean(modification));

                                            String chatMessage;
                                            if (user.isVanished()) {
                                                chatMessage = "**" + player.getName() + " left the game**";
                                            } else {
                                                chatMessage = "**" + player.getName() + " joined the game**";
                                            }
                                            // TODO: Send the message to the chat channel.
//                      Bot.getGameChatChannel().sendMessage(chatMessage).queue();
//                      Bot.sendLog(chatMessage);

                                            Messager.send(callee, Component.text("Discord Vanish is currently set to ").color(NamedTextColor.GRAY)
                                                    .append(Component.text(user.isVanished()).color(NamedTextColor.WHITE)));
                                        }
                                        case "autoinvswap" -> {
                                            if (!player.hasPermission("dreamvisitor.set.autoinvswap"))
                                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                            user.setAutoInvSwapEnabled(Boolean.parseBoolean(modification));
                                            Messager.send(callee, Component.text("Automatic Inventory is currently set to ").color(NamedTextColor.GRAY)
                                                    .append(Component.text(user.isAutoInvSwapEnabled()).color(NamedTextColor.WHITE)));
                                        }
                                        case "autoradio" -> {
                                            if (!player.hasPermission("dreamvisitor.set.autoradio"))
                                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                            user.setAutoRadioEnabled(Boolean.parseBoolean(modification));
                                            Messager.send(callee, Component.text("Automatic Radio is currently set to ").color(NamedTextColor.GRAY)
                                                    .append(Component.text(user.isAutoRadioEnabled()).color(NamedTextColor.WHITE)));
                                        }
                                        default ->
                                                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                                    }
                                    sendUserGui(player);
                                }))));
    }
}
