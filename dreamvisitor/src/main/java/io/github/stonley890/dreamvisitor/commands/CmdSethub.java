package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.wrappers.Rotation;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdSethub implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("sethub")
                .withPermission(CommandPermission.OP)
                .withHelp("Set the hub location.", "Set the location to teleport to when executing /hub.")
                .withOptionalArguments(new LocationArgument("location"))
                .withOptionalArguments(new RotationArgument("rotation"))
                .withOptionalArguments(new WorldArgument("world"))
                .executesNative((sender, args) -> {
                    Location location = (Location) args.get("location");
                    Rotation rotation = (Rotation) args.get("rotation");
                    World world = (World) args.get("world");
                    CommandSender callee = sender.getCallee();
                    if (location == null) {
                        if (callee instanceof Entity entity) {
                            location = entity.getLocation();
                        } else if (callee instanceof BlockCommandSender block) {
                            location = block.getBlock().getLocation();
                        } else {
                            throw CommandAPI.failWithString("Location must be specified when it cannot be inferred!");
                        }
                    }
                    if (rotation != null) {
                        location.setYaw(rotation.getYaw());
                        location.setPitch(rotation.getPitch());
                    }
                    if (world == null) {
                        if (callee instanceof Entity entity) {
                            world = entity.getLocation().getWorld();
                        } else if (callee instanceof BlockCommandSender block) {
                            world = block.getBlock().getLocation().getWorld();
                        } else {
                            throw CommandAPI.failWithString("World must be specified when it cannot be inferred!");
                        }
                    }

                    location.setWorld(world);

                    Dreamvisitor.hubLocation = location;
                    plugin.getConfig().set("hubLocation", Dreamvisitor.hubLocation);
                    plugin.saveConfig();
                    Messager.send(sender, "Hub location set.");
                });
    }
}
