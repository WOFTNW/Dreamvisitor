package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.WorldArgument;
import org.jetbrains.annotations.Nullable;
import org.woftnw.dreamvisitor.functions.Messager;
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
                    @Nullable World targetWorld = (World) args.get("world");
                    final CommandSender callee = sender.getCallee();
                    if (targetWorld == null) {
                        if (callee instanceof Entity entity) {
                            targetWorld = entity.getWorld();
                        } else if (callee instanceof BlockCommandSender block) {
                            targetWorld = block.getBlock().getWorld();
                        } else throw CommandAPI.failWithString("World must be specified if it cannot be inferred!");
                    }
                    for (World world : Bukkit.getWorlds()) world.setFullTime(targetWorld.getFullTime());
                    Messager.send(sender, "Set all worlds to match " + targetWorld.getName() + ": " + targetWorld.getFullTime());
                });
    }
}
