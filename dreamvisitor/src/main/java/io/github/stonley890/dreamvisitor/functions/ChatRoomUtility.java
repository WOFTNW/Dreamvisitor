package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ChatRoomUtility {

	/**
	 * Get an {@link ArrayList} of all players in a given chat room.
	 * @param room the name of the chat room to check
	 * @return the online players in that chatroom
	 */
	@NotNull
	public static ArrayList<? extends Player> getPlayersInRoom(String room) {
		// Create a list of online players
		ArrayList<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
		// Remove players not in the room
		players.removeIf((player) -> {
			PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
			return memory.chatRoom == null || !memory.chatRoom.equals(room);
		});

		return players;
	}

	/**
	 * Get an {@link ArrayList} of all rooms in use.
	 * @return a string list of rooms in use.
	 */
	@NotNull
	public static ArrayList<String> getActiveRooms() {
		ArrayList<String> rooms = new ArrayList<>();

		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for (Player player : players) {
			PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
			String chatRoom = memory.chatRoom;
			if (chatRoom != null && !rooms.contains(chatRoom)) {
				rooms.add(chatRoom);
			}
		}

		return rooms;
	}

	/**
	 * Put a player in a chat room.
	 * @param player the player to put in a room.
	 * @param room the name of the room to put them in.
	 */
	public static void putPlayerInRoom(@NotNull Player player, String room) {
		UUID uuid = player.getUniqueId();
		PlayerMemory playerMemory = PlayerUtility.getPlayerMemory(uuid);
		playerMemory.chatRoom = room;
		try {
			PlayerUtility.savePlayerMemory(uuid);
		} catch (IOException e) {
			// Could not be saved for some reason. Should be fine in this
			// case since chat rooms should not persist past a single
			// session.
		}
	}

	/**
	 * Remove a player from their chatroom, if they're in one.
	 * @param player the player to remove.
	 */
	public static void removePlayerFromRoom(@NotNull Player player) {
		UUID uuid = player.getUniqueId();
		PlayerMemory playerMemory = PlayerUtility.getPlayerMemory(uuid);
		playerMemory.chatRoom = null;
		try {
			PlayerUtility.savePlayerMemory(uuid);
		} catch (IOException e) {
			// Could not be saved for some reason. Should be fine in this
			// case since chat rooms should not persist past a single
			// session.
		}
	}

	/**
	 * Get the chat room a player is in.
	 * @param player the player to check
	 * @return the name of the chat room or null if not in one.
	 */
	public static String getPlayerChatRoom(@NotNull Player player) {
		UUID uuid = player.getUniqueId();
		PlayerMemory playerMemory = PlayerUtility.getPlayerMemory(uuid);
		return playerMemory.chatRoom;
	}
}
