package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import org.woftnw.dreamvisitor.data.Tribe;
import org.woftnw.dreamvisitor.functions.Mail;
import org.woftnw.dreamvisitor.functions.Messager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CmdParcel implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("parcel")
                .withHelp("Manage the mail system.", "Manage the mail economy mini-game system.")
                .withSubcommand(new CommandAPICommand("cancel")
                        .withPermission("dreamvisitor.mail.play")
                        .executesPlayer((sender, args) -> {
                            if (!Mail.isPLayerDeliverer(sender)) throw CommandAPI.failWithString("You are not currently delivering!");
                            Mail.cancel(sender);
                        })
                )
                .withSubcommand(new CommandAPICommand("locations")
                        .withPermission(CommandPermission.fromString("dreamvisitor.mail.manage"))
                        .withSubcommand(new CommandAPICommand("add")
                                .withArguments(new LocationArgument("location"))
                                .withArguments(new StringArgument("name"))
                                .withArguments(new IntegerArgument("weight", 0))
                                .withArguments(CommandUtils.customTribeArgument("homeTribe"))
                                .executesNative(((sender, args) -> {

                                    final Location location = (Location) args.get("location");
                                    if (location == null) throw CommandAPI.failWithString("Location was not provided!");

                                    final String name = (String) args.get("name");
                                    if (name == null) throw CommandAPI.failWithString("Name was not provided!");
                                    final Object weightArg = args.get("weight");
                                    if (weightArg == null) throw CommandAPI.failWithString("Weight was not provided!");
                                    int weight = (int) weightArg;
                                    final Tribe tribe = (Tribe) args.get("homeTribe");
                                    if (tribe == null) throw CommandAPI.failWithString("Tribe was not provided!");

                                    Mail.MailLocation mailLocation = new Mail.MailLocation(location, name, weight, tribe);
                                    Mail.saveLocation(mailLocation);

                                    Messager.send(sender, "Added location " + name + " at " + location.getX() + " " + location.getY() + " " + location.getZ() + " in world " + Objects.requireNonNull(location.getWorld()).getName() + ".");
                                }))
                        )
                        .withSubcommand(new CommandAPICommand("remove")
                                .withArguments(new StringArgument("name")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                                .executesNative((sender, args) -> {

                                    final String name = (String) args.get("name");
                                    if (name == null) throw CommandAPI.failWithString("Name is null!");

                                    final Mail.MailLocation location = Mail.getLocationByName(name);
                                    if (location == null) throw CommandAPI.failWithString("Mail location not found.");
                                    Mail.removeLocation(location);
                                    Messager.send(sender, "Removed location " + location.getName());
                                })
                        )
                        .withSubcommand(new CommandAPICommand("list")
                                .executesNative((sender, args) -> {
                                    List<Mail.MailLocation> locations = Mail.getLocations();

                                    final ComponentBuilder message = new ComponentBuilder("Mail Locations");

                                    for (Mail.MailLocation mailLocation : locations) {
                                        message.append("\n").append(mailLocation.getName()).color(net.md_5.bungee.api.ChatColor.YELLOW)
                                                .append("\n").reset().append(String.valueOf(mailLocation.getLocation().getX()))
                                                .append(" ").append(String.valueOf(mailLocation.getLocation().getY()))
                                                .append(" ").append(String.valueOf(mailLocation.getLocation().getZ()))
                                                .append(" in world ").append(Objects.requireNonNull(mailLocation.getLocation().getWorld()).getName())
                                                .append("\n Weight: ").append(String.valueOf(mailLocation.getWeight()))
                                                .append("\n Home Tribe: ").append(mailLocation.getHomeTribe().getName());
                                    }
                                    Messager.send(sender, message.create());
                                })
                        )
                )
                .withSubcommand(new CommandAPICommand("delivery")
                        .withPermission(CommandPermission.fromString("dreamvisitor.mail.manage"))
                        .withSubcommand(new CommandAPICommand("terminal")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                                .executesNative((sender, args) -> {
                                    final Collection<Player> players = (Collection<Player>) args.get("players");
                                    assert players != null;

                                    if (players.isEmpty()) throw CommandAPI.failWithString("No players selected");

                                    final List<Mail.Deliverer> deliverers = Mail.getDeliverers();
                                    final List<Player> playerList = deliverers.stream().map(Mail.Deliverer::getPlayer).toList();

                                    final List<Player> addPlayers = new ArrayList<>();
                                    final List<Player> removePlayers = new ArrayList<>();

                                    for (Player player : players) {
                                        if (playerList.contains(player)) removePlayers.add(player);
                                        else addPlayers.add(player);
                                    }

                                    Mail.MailLocation startLoc = Mail.getNearestLocation(sender.getLocation());
                                    if (startLoc == null) throw CommandAPI.failWithString("No start mail location found.");

                                    for (Player player : addPlayers) {

                                        final Mail.MailLocation endLoc;

                                        try {
                                            endLoc = Mail.chooseDeliveryLocation(startLoc);
                                        } catch (InvalidConfigurationException e) {
                                            throw CommandAPI.failWithString("Could not choose delivery location: " + e.getMessage());
                                        }

                                        add(Collections.singleton(player), startLoc, endLoc);
                                    }

                                    for (Player player : removePlayers) {
                                        try {
                                            Mail.complete(player);
                                        } catch (Exception e) {
                                            final String message = e.getMessage();
                                            switch (message) {
                                                case "Player does not have parcel!" ->
                                                        Messager.sendDanger(player, ChatColor.RED + "You do not have the parcel that is to be delivered!");
                                                case "EssentialsX is not currently active!" ->
                                                        Messager.sendDanger(player, ChatColor.RED + "EssentialsX is not enabled! Contact a staff member!");
                                                case "Not at the destination location!" ->
                                                        Messager.sendDanger(player, ChatColor.RED + "This is not your delivery location!");
                                                default ->
                                                        Messager.sendDanger(player, ChatColor.RED + "There was a problem: " + message + "\nPlease contact a staff member.");
                                            }
                                            return;
                                        }
                                    }

                                    Messager.send(sender, "Toggled mail for " + players.size() + ".");
                                })
                        )

                        .withSubcommand(new CommandAPICommand("add")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                                .withArguments(new StringArgument("start")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                                .withArguments(new StringArgument("end")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                                .executesNative((sender, args) -> {

                                    final String startArg = (String) args.get("start");
                                    final Mail.MailLocation start = Mail.getLocationByName(startArg);
                                    if (start == null) throw CommandAPI.failWithString(startArg + " is not a valid MailLocation!");

                                    final String endArg = (String) args.get("end");
                                    final Mail.MailLocation end = Mail.getLocationByName(endArg);
                                    if (end == null) throw CommandAPI.failWithString(endArg + " is not a valid MailLocation!");

                                    final Collection<Player> players = (Collection<Player>) args.get("players");
                                    assert players != null;

                                    if (players.isEmpty()) throw CommandAPI.failWithString("No players selected");

                                    add(players, start, end);
                                    Messager.send(sender, "Added " + Messager.nameOrCountPlayer(players) + ".");
                                })
                        )

                        .withSubcommand(new CommandAPICommand("remove")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players")
                                )
                                .executesNative((sender, args) -> {
                                    Collection<Player> players = (Collection<Player>) args.get("players");
                                    assert players != null;

                                    if (players.isEmpty()) throw CommandAPI.failWithString("No players selected");

                                    remove(players);
                                    Messager.send(sender, "Removed " + Messager.nameOrCountPlayer(players) + ".");
                                })
                        )

                        .withSubcommand(new CommandAPICommand("list")
                                .executesNative((sender, args) -> {
                                    final StringBuilder message = new StringBuilder();
                                    for (Mail.Deliverer deliverer : Mail.getDeliverers()) {
                                        message.append(deliverer.getPlayer().getName()).append(" ");
                                    }
                                    Messager.send(sender, message.toString());
                                })
                        )


                );
    }

    private void add(@NotNull Collection<Player> players, @NotNull Mail.MailLocation start, @NotNull Mail.MailLocation end) {
        List<Mail.Deliverer> deliverers = Mail.getDeliverers();
        Messager.debug("Size of deliverers: " + deliverers.size());

        for (Player player : players) {
            Messager.debug("Adding player " + player.getName());
            final Mail.Deliverer deliverer = new Mail.Deliverer(player, start, end);
            deliverer.start();
            deliverers.add(deliverer);
            Messager.send(player, "Deliver this parcel to " + ChatColor.YELLOW + end.getName().replace("_", " ") + ChatColor.WHITE + ".\nRun " + ChatColor.AQUA + "/" + getCommand().getName() + " cancel" + ChatColor.WHITE + " to cancel.");
        }

        Messager.debug("Size of deliverers now: " + deliverers.size());
        Mail.setDeliverers(deliverers);
    }

    private void remove(@NotNull Collection<Player> players) {
        final List<Mail.Deliverer> deliverers = Mail.getDeliverers();
        deliverers.removeIf(deliverer -> players.contains(deliverer.getPlayer()));
        Mail.setDeliverers(deliverers);
    }
}
