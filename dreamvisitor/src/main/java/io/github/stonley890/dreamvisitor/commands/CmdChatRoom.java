package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.ChatRoomUtility;
import io.github.stonley890.dreamvisitor.functions.Chatback;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class CmdChatRoom implements DVCommand {
	@NotNull
	@Override
	public ExecutableCommand<?, ?> getCommand() {
		return new CommandAPICommand("chatroom")
				.withPermission(CommandPermission.fromString("dreamvisitor.chatroom"))
				.withSubcommands(
						new CommandAPICommand("enter")
								.withHelp("Enter a chat rooms", "Enter a new or existing chat room.")
								.withArguments(
										new StringArgument("room")
								)
								.executesPlayer((sender, args) -> {

									String room = (String) args.get("room");
									ChatRoomUtility.putPlayerInRoom(sender, room);

									int playersInRoom = ChatRoomUtility.getPlayersInRoom(room).size();
									int playerInRoomBeforeJoin = playersInRoom - 1;
									String playerSingOrPlur = "players";
									if (playerInRoomBeforeJoin == 1) playerSingOrPlur = "player";

									// e.g. Joined room "jma" with 5 other players.
									sender.sendMessage(Dreamvisitor.PREFIX + "Joined room \"" + room + "\" with " + playerInRoomBeforeJoin + " other " + playerSingOrPlur + ".");
								}),
						new CommandAPICommand("exit")
								.withHelp("Exit chat room", "Exit any chat room you are currently in and return to the main chat.")
								.executesPlayer((sender, args) -> {

									ChatRoomUtility.removePlayerFromRoom(sender);

									// e.g. Joined room "jma" with 5 other players.
									sender.sendMessage(Dreamvisitor.PREFIX + "Left chat room and returned to main chat.");
								}),
						new CommandAPICommand("list")
								.withHelp("List chat rooms", "List all chat rooms currently in use.")
								.withPermission(CommandPermission.fromString("dreamvisitor.chatroom.list"))
								.executesPlayer((sender, args) -> {

									ArrayList<String> rooms = ChatRoomUtility.getActiveRooms();

									if (rooms.isEmpty()) {
										sender.sendMessage(Dreamvisitor.PREFIX + "There are no rooms in use.");
										return;
									}

									sender.sendMessage(Dreamvisitor.PREFIX + String.join(", ", rooms));
								}),
						new CommandAPICommand("peek")
								.withHelp("See who is in a chat room", "Get a list of players in a given chat room.")
								.withPermission(CommandPermission.fromString("dreamvisitor.chatroom.peek"))
								.withArguments(
										new StringArgument("room")
								)
								.executesPlayer((sender, args) -> {

									String room = (String) args.get("room");

									ArrayList<? extends Player> playersInRoom = ChatRoomUtility.getPlayersInRoom(room);

									if (playersInRoom.isEmpty()) {
										sender.sendMessage(Dreamvisitor.PREFIX + "There are no players in that room.");
										return;
									}

									List<String> playerNames = playersInRoom.stream().map(Player::getName).toList();

									sender.sendMessage(Dreamvisitor.PREFIX + String.join(", ", playerNames));
								}),
						new CommandAPICommand("invite")
								.withHelp("Invite a player", "Invite a player to your chat room.")
								.withPermission(CommandPermission.fromString("dreamvisitor.chatroom.invite"))
								.withArguments(
										new PlayerArgument("player")
								)
								.executesPlayer((sender, args) -> {

									Player invitedPlayer = (Player) args.get("player");

									if (invitedPlayer == null) {
										throw CommandAPI.failWithString("That player is null. This should never happen :(");
									}

									String chatRoom = ChatRoomUtility.getPlayerChatRoom(sender);

									if (chatRoom == null) {
										throw CommandAPI.failWithString("You are not currently in a chat room. Try /chatroom enter <room>");
									}

									ComponentBuilder builder = new ComponentBuilder();

									String joinCommand = "/chatroom enter " + chatRoom;

									TextComponent joinButton = new TextComponent("here");
									joinButton.setColor(ChatColor.AQUA);
									ClickEvent joinButtonClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, joinCommand);
									joinButton.setClickEvent(joinButtonClickEvent);

									builder
											.append(Dreamvisitor.PREFIX)
											.append(sender.getName())
											.append(" invited you to join the \"")
											.append(chatRoom)
											.append("\" chat room. Click ")
											.append(joinButton)
											.append(" or run ").reset()
											.append(joinCommand).color(ChatColor.AQUA)
											.append(" to join.").reset();

									invitedPlayer.spigot().sendMessage(builder.build());


									sender.sendMessage(Dreamvisitor.PREFIX + "Sent invite to " + invitedPlayer.getName() + ".");
								})
				);
	}
}
