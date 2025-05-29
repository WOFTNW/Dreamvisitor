package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.NativeCommandExecutor;
import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CmdDvset implements DVCommand {

  private static void sendUserGui(@NotNull Player player) {

    PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

    ComponentBuilder builder = new ComponentBuilder(Dreamvisitor.PREFIX);
    builder.append("User Options ");

    if (player.hasPermission("dreamvisitor.set.discord")) {
      builder.append("\n\nDiscord Visibility: ").color(ChatColor.WHITE)
          .append("\nWhether to show messages from Discord's chat bridge.").color(ChatColor.GRAY)
          .append("\n[").color(ChatColor.DARK_GRAY)
          .append(booleanToggle(memory.discordEnabled, "discord"))
          .append("").reset().append("]").color(ChatColor.DARK_GRAY);
    }

    if (player.hasPermission("dreamvisitor.set.flight")) {
      builder.append("\n\nFlight Disabled: ").color(ChatColor.WHITE)
          .append("\nWhether Flight Mode cannot be activated.").color(ChatColor.GRAY)
          .append("\n[").color(ChatColor.DARK_GRAY)
          .append(booleanToggle(memory.flightDisabled, "flight"))
          .append("").reset().append("]").color(ChatColor.DARK_GRAY);
    }

    if (player.hasPermission("dreamvisitor.set.zoop")) {
      builder.append("\n\nDiscord Vanish: ").color(ChatColor.WHITE)
          .append("\nWhether to appear offline in Dreamvisitor.").color(ChatColor.GRAY)
          .append("\n[").color(ChatColor.DARK_GRAY)
          .append(booleanToggle(memory.vanished, "vanished"))
          .append("").reset().append("]").color(ChatColor.DARK_GRAY);
    }

    if (player.hasPermission("dreamvisitor.set.autoinvswap")) {
      builder.append("\n\nAutomatic Inventory Swap: ").color(ChatColor.WHITE)
          .append("\nWhether to automatically swap inventories on game mode change.").color(ChatColor.GRAY)
          .append("\n[").color(ChatColor.DARK_GRAY)
          .append(booleanToggle(memory.autoinvswap, "autoinvswap"))
          .append("").reset().append("]").color(ChatColor.DARK_GRAY);
    }

    if (player.hasPermission("dreamvisitor.set.autoradio")) {
      builder.append("\n\nAutomatic Radio: ").color(ChatColor.WHITE)
          .append("\nWhether to send all messages to staff radio.").color(ChatColor.GRAY)
          .append("\n[").color(ChatColor.DARK_GRAY)
          .append(booleanToggle(memory.autoRadio, "autoradio"))
          .append("").reset().append("]").color(ChatColor.DARK_GRAY);
    }

    builder.append("").reset().append("\n");
    player.spigot().sendMessage(builder.create());

  }

  /**
   * Creates a {@link TextComponent} representing a {@code boolean} value with a
   * command to change it.
   *
   * @param value   the {@code boolean} to display.
   * @param cmdName the command to run. This will be formatted as
   *                {@code /dvset <state> <cmdName> !value}
   * @return a {@link TextComponent} representing the value with a command to
   *         change it.
   */
  private static @NotNull TextComponent booleanToggle(boolean value, String cmdName) {
    TextComponent toggle = new TextComponent();
    toggle.setUnderlined(true);

    if (value) {
      toggle.setText("-O");
      toggle.setColor(ChatColor.GREEN);
    } else {
      toggle.setText("O-");
      toggle.setColor(ChatColor.RED);
    }

    toggle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
        ChatColor.GRAY + "Currently toggled to " + ChatColor.WHITE + String.valueOf(value).toUpperCase() + ".")));
    toggle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
        "/dvset " + cmdName + " " + String.valueOf(!value).toLowerCase()));

    return toggle;
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
            .executesNative((NativeCommandExecutor) ((sender, args) -> {
              String option = (String) args.get("option");

              CommandSender callee = sender.getCallee();
              if (!callee.hasPermission("dreamvisitor.userset"))
                throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");

              PlayerMemory playerMemory;

              if (callee instanceof Player player) {
                playerMemory = PlayerUtility.getPlayerMemory(player.getUniqueId());
              } else
                throw CommandAPI.failWithString("This can only be executed by a player!");

              switch (Objects.requireNonNull(option)) {
                case "discord" -> {
                  if (!player.hasPermission("dreamvisitor.set.discord"))
                    throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                  callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Visibility is currently set to "
                      + ChatColor.WHITE + playerMemory.discordEnabled);
                }
                case "flight" -> {
                  if (!player.hasPermission("dreamvisitor.set.flight"))
                    throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                  callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Flight Enabled is currently set to "
                      + ChatColor.WHITE + playerMemory.flightDisabled);
                }
                case "vanished" -> {
                  if (!player.hasPermission("dreamvisitor.set.zoop"))
                    throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                  callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Vanish is currently set to "
                      + ChatColor.WHITE + playerMemory.vanished);
                }
                case "autoinvswap" -> {
                  if (!player.hasPermission("dreamvisitor.set.autoinvswap"))
                    throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                  callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY
                      + "Automatic Inventory Swap is currently set to " + ChatColor.WHITE + playerMemory.autoinvswap);
                }
                case "autoradio" -> {
                  if (!player.hasPermission("dreamvisitor.set.autoradio"))
                    throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                  callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Radio is currently set to "
                      + ChatColor.WHITE + playerMemory.autoRadio);
                }
                default ->
                  throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
              }
            }))
            .then(new StringArgument("modification")
                .setOptional(true)
                .executesNative(((sender, args) -> {
                  String option = (String) args.get("option");
                  String modification = (String) args.get("modification");

                  CommandSender callee = sender.getCallee();
                  if (!callee.hasPermission("dreamvisitor.userset"))
                    throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");

                  PlayerMemory playerMemory;

                  if (callee instanceof Player player) {
                    playerMemory = PlayerUtility.getPlayerMemory(player.getUniqueId());
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
                      playerMemory.discordEnabled = Boolean.parseBoolean(modification);
                      callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Visibility toggled to "
                          + ChatColor.WHITE + playerMemory.discordEnabled);
                    }
                    case "flight" -> {
                      if (!player.hasPermission("dreamvisitor.set.flight"))
                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                      playerMemory.flightDisabled = Boolean.parseBoolean(modification);
                      callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Flight Enabled toggled to "
                          + ChatColor.WHITE + playerMemory.flightDisabled);
                    }
                    case "vanished" -> {
                      if (!player.hasPermission("dreamvisitor.set.zoop"))
                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                      playerMemory.vanished = Boolean.parseBoolean(modification);

                      String chatMessage;
                      if (playerMemory.vanished) {
                        chatMessage = "**" + player.getName() + " left the game**";
                      } else {
                        chatMessage = "**" + player.getName() + " joined the game**";
                      }
                      Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                      Bot.sendLog(chatMessage);

                      callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Vanish toggled to "
                          + ChatColor.WHITE + playerMemory.vanished);
                    }
                    case "autoinvswap" -> {
                      if (!player.hasPermission("dreamvisitor.set.autoinvswap"))
                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                      playerMemory.autoinvswap = Boolean.parseBoolean(modification);
                      callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Inventory Swap toggled to "
                          + ChatColor.WHITE + playerMemory.autoinvswap);
                    }
                    case "autoradio" -> {
                      if (!player.hasPermission("dreamvisitor.set.autoradio"))
                        throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                      playerMemory.autoRadio = Boolean.parseBoolean(modification);
                      callee.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Radio toggled to "
                          + ChatColor.WHITE + playerMemory.autoRadio);
                    }
                    default ->
                      throw CommandAPI.failWithString("Invalid arguments or insufficient permissions!");
                  }
                  sendUserGui(player);
                }))));
  }
}
