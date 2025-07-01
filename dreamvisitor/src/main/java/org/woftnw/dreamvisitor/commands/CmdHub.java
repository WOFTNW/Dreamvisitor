package org.woftnw.dreamvisitor.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.woftnw.dreamvisitor.Dreamvisitor;
import org.woftnw.dreamvisitor.functions.Mail;
import org.woftnw.dreamvisitor.functions.Messager;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CmdHub implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("hub")
                .withHelp("Teleport to the hub.", "Teleport entities to the hub location.")
                .withPermission(CommandPermission.fromString("dreamvisitor.hub"))
                .withOptionalArguments(new EntitySelectorArgument.ManyEntities("entities")
                        .withPermission(CommandPermission.fromString("dreamvisitor.hub.select"))
                )
                .executesNative(((sender, args) -> {

                    final Collection<Entity> entitySelect = (Collection<Entity>) args.get("entities");

                    final CommandSender callee = sender.getCallee();
                    if (entitySelect != null) {

                        if (entitySelect.isEmpty()) {
                            throw CommandAPI.failWithString("No targets selected.");
                        } else {
                            if (plugin.getConfig().getLocation("hubLocation") == null) {
                                throw CommandAPI.failWithString("No hub is currently set!");
                            } else {

                                Dreamvisitor.hubLocation = plugin.getConfig().getLocation("hubLocation");
                                assert Dreamvisitor.hubLocation != null;

                                final Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

                                for (Entity entity : entitySelect) {

                                    if (entity instanceof Player player) {

                                        if (Mail.isPLayerDeliverer(player)) Mail.cancel(player);

                                        // Set /back location
                                        if (ess != null) {
                                            User user = ess.getUser(player);
                                            user.setLastLocation(player.getLocation());
                                        }

                                        player.teleport(Dreamvisitor.hubLocation);
                                        player.spawnParticle(Particle.FIREWORK, Dreamvisitor.hubLocation, 100);
                                        player.playSound(Dreamvisitor.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f, 1f);
                                    } else entity.teleport(Dreamvisitor.hubLocation, TeleportCause.COMMAND);
                                }
                            }
                            if (entitySelect.size() == 1) {
                                Messager.send(callee, "Teleported " + entitySelect.stream().findFirst().get().getName() + " to the hub.");
                            }
                            else {
                                Messager.send(callee, "Teleported " + entitySelect.size() + " entities to the hub.");
                            }
                        }

                    } else {
                        if (callee instanceof Player player) {
                            if (plugin.getConfig().getLocation("hubLocation") == null) {
                                throw CommandAPI.failWithString("No hub is currently set!");
                            } else {

                                Dreamvisitor.hubLocation = plugin.getConfig().getLocation("hubLocation");
                                assert Dreamvisitor.hubLocation != null;

                                if (Mail.isPLayerDeliverer(player)) Mail.cancel(player);

                                final Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                                if (ess != null) {
                                    final User user = ess.getUser(player);
                                    user.setLastLocation(player.getLocation());
                                }

                                final List<LivingEntity> leashed = new ArrayList<>();

                                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                                    if (entity instanceof LivingEntity livingEntity && livingEntity.isLeashed() && livingEntity.getLeashHolder().equals(player))
                                        leashed.add(livingEntity);
                                }

                                if (leashed.isEmpty() || !player.hasPermission("dreamvisitor.hub.leash")) {
                                    player.teleport(Dreamvisitor.hubLocation);
                                    player.spawnParticle(Particle.FIREWORK, Dreamvisitor.hubLocation, 100);
                                    player.playSound(Dreamvisitor.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                                            1f);
                                } else {

                                    final Location tpLocation = Dreamvisitor.hubLocation.clone().subtract(0, 14, 0);

                                    player.teleport(tpLocation);
                                    player.spawnParticle(Particle.FIREWORK, tpLocation, 100);
                                    player.playSound(tpLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                                            1f);
                                    for (LivingEntity entity : leashed) {
                                        entity.teleport(tpLocation);
                                    }
                                }
                            }

                        } else if (callee instanceof BlockCommandSender cmdblock) {

                            // Find the closest player
                            double lastDistance = 10;
                            Player closest = null;

                            for (Entity entity : cmdblock.getBlock().getWorld().getNearbyEntities(cmdblock.getBlock().getLocation(), 10, 10, 10)) {
                                if (entity instanceof Player) {
                                    double distance = entity.getLocation().distance(cmdblock.getBlock().getLocation());
                                    if (distance < lastDistance) {
                                        lastDistance = distance;
                                        closest = (Player) entity;
                                    }
                                }
                            }

                            if (closest != null) {
                                if (plugin.getConfig().getLocation("hubLocation") == null) {
                                    throw CommandAPI.failWithString("No hub is currently set!");
                                } else {

                                    if (Mail.isPLayerDeliverer(closest)) Mail.cancel(closest);

                                    Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                                    if (ess != null) {
                                        User user = ess.getUser(closest);
                                        user.setLastLocation(closest.getLocation());
                                    }

                                    closest.teleport(Dreamvisitor.hubLocation);
                                    closest.spawnParticle(Particle.FIREWORK, Dreamvisitor.hubLocation, 100);
                                    closest.playSound(Dreamvisitor.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                                            1f);
                                }
                            }

                        } else {
                            throw CommandAPI.failWithString("Missing arguments! /hub [targets]");
                        }
                    }

                }));
    }
}
