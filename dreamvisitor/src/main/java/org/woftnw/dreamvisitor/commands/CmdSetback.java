package org.woftnw.dreamvisitor.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.wrappers.Rotation;
import org.jetbrains.annotations.Nullable;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class CmdSetback implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("setback")
                .withPermission(CommandPermission.fromString("dreamvisitor.setback"))
                .withHelp("Set a player's last location.", "Set a player's last EssentialsX location.")
                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                .withOptionalArguments(new LocationArgument("location", LocationType.PRECISE_POSITION))
                .withOptionalArguments(new RotationArgument("rotation"))
                .withOptionalArguments(new WorldArgument("world"))
                .executesNative((sender, args) -> {

                    final Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                    if (ess == null) {
                        throw CommandAPI.failWithString("EssentialsX is not currently active!");
                    }

                    final Collection<Player> players = (Collection<Player>) args.get("players");
                    @Nullable Location location = (Location) args.get("location");
                    @Nullable Rotation rotation = (Rotation) args.get("rotation");
                    @Nullable World world = (World) args.get("world");

                    assert players != null;

                    final CommandSender callee = sender.getCallee();
                    if (location == null) {
                        if (callee instanceof Entity entity) {
                            location = entity.getLocation();
                        } else if (callee instanceof BlockCommandSender block) {
                            location = block.getBlock().getLocation();
                        } else throw CommandAPI.failWithString("You must specify a location!");
                    }

                    if (rotation != null) {
                        location.setPitch(rotation.getPitch());
                        location.setYaw(rotation.getYaw());
                    }

                    if (world == null) {
                        if (callee instanceof Entity entity) {
                            world = entity.getWorld();
                        } else if (callee instanceof BlockCommandSender block) {
                            world = block.getBlock().getWorld();
                        } else throw CommandAPI.failWithString("You must specify a world!");
                    }

                    location.setWorld(world);

                    for (Player player : players) {
                        User user = ess.getUser(player);
                        user.setLastLocation(location);
                    }

                    Messager.send(sender, "Set back location to " + location.getBlockX() + ", " + location.getBlockY() +
                            ", " + location.getBlockZ() + " of " + Objects.requireNonNull(location.getWorld()).getName() + " for " +
                            players.size() + " player(s).");

                });
    }
}
