package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import io.github.stonley890.dreamvisitor.functions.Messager;
import io.github.stonley890.dreamvisitor.functions.Moonglobe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class CmdMoonglobe implements DVCommand {

    @NotNull
    @Contract(pure = true)
    private static String create(@NotNull Collection<Player> players, @NotNull Location location, float maxDistance) {

        for (Player player : players) {
            boolean alreadyHasGlobe = false;
            for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                if (Objects.equals(activeMoonglobe.getPlayer(), player.getUniqueId())) alreadyHasGlobe = true;
            }
            if (!alreadyHasGlobe) new Moonglobe(player.getUniqueId(), location, maxDistance);
        }

        return ("Created moon globes for " + players.size() + " players.");

    }

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("moonglobe")
                .withPermission(CommandPermission.fromString("dreamvisitor.moonglobes"))
                .withShortDescription("Manages moon globes.")
                .withFullDescription("Create and remove moon globes to/from players.")
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executesNative((sender, args) -> {

                            Collection<Player> targets = (Collection<Player>) args.get("players");

                            // Get players
                            assert targets != null;

                            for (Player player : targets) {
                                for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                                    if (Objects.equals(activeMoonglobe.getPlayer(), player.getUniqueId())) activeMoonglobe.remove(null);
                                }
                            }

                            Messager.send(sender, "Removed moon globes of " + Messager.nameOrCountPlayer(targets) + ".");
                        })
                )
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .withOptionalArguments(new LocationArgument("location"))
                        .withOptionalArguments(new FloatArgument("maxDistance"))
                        .executesNative((sender, args) -> {
                            Collection<Player> targets = (Collection<Player>) args.get("players");

                            // Get players
                            assert targets != null;

                            Location location = getLocation(sender, args);

                            // get distance
                            float maxDistance = 256.0f;

                            Object maxDistanceArg = args.get("maxDistance");
                            if (maxDistanceArg != null) maxDistance = (float) maxDistanceArg;

                            Messager.send(sender, create(targets, location, maxDistance));
                        })
                        .executesNative((sender, args) -> {

                            Collection<Player> targets = (Collection<Player>) args.get("players");

                            // Get players
                            assert targets != null;

                            World world;

                            CommandSender callee = sender.getCallee();
                            if (callee instanceof Player player) world = player.getWorld();
                            else if (callee instanceof BlockCommandSender block) world = block.getBlock().getWorld();
                            else world = Bukkit.getWorlds().get(0);

                            // Get location
                            Location location = (Location) args.get("location");
                            if (location == null) {
                                double x; double y; double z;
                                if (callee instanceof Player player) {
                                    x = player.getLocation().getX();
                                    y = player.getLocation().getY();
                                    z = player.getLocation().getZ();
                                } else if (callee instanceof BlockCommandSender block) {
                                    x = block.getBlock().getX();
                                    y = block.getBlock().getY();
                                    z = block.getBlock().getZ();
                                }
                                else {
                                    throw CommandAPI.failWithString("You must specify a location; it could not be inferred!");
                                }
                                location = new Location(world, x, y, z);
                            }

                            // get distance
                            float maxDistance = 256.0f;

                            Object maxDistanceArg = args.get("maxDistance");
                            if (maxDistanceArg != null) maxDistance = (float) maxDistanceArg;

                            Messager.send(sender, create(targets, location, maxDistance));
                        })
                );
    }

    private static @NotNull Location getLocation(NativeProxyCommandSender sender, CommandArguments args) {
        World world;

        if (sender instanceof Player player) world = player.getWorld();
        else if (sender instanceof BlockCommandSender block) world = block.getBlock().getWorld();
        else world = Bukkit.getWorlds().get(0);

        // Get location
        Location location = (Location) args.get("location");
        if (location == null) {
            double x; double y; double z;

            x = sender.getLocation().getX();
            y = sender.getLocation().getY();
            z = sender.getLocation().getZ();

            location = new Location(world, x, y, z);
        }
        return location;
    }
}
