package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.WorldArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Messager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class CmdSynctime implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("synctime")
                .withPermission(CommandPermission.fromString("dreamvisitor.synctime"))
                .withHelp("Sync time across worlds.", "Sync time across all worlds.")
                .withOptionalArguments(new WorldArgument("world"))
                .executesNative((sender, args) -> {
                    World worldArg = (World) args.get("world");
                    CommandSender callee = sender.getCallee();
                    if (worldArg == null) {
                        if (callee instanceof Entity entity) {
                            worldArg = entity.getWorld();
                        } else if (callee instanceof BlockCommandSender block) {
                            worldArg = block.getBlock().getWorld();
                        } else throw CommandAPI.failWithString("World must be specified if it cannot be inferred!");
                    }
                    for (World world : Bukkit.getWorlds()) world.setFullTime(worldArg.getFullTime());
                    Messager.send(sender, "Set all worlds to match " + worldArg.getName() + ": " + worldArg.getFullTime());
                });
    }
}
