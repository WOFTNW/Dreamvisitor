package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.data.TribeUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CmdDreamvisitor implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("dreamvisitor")
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.BLUE + "Dreamvisitor " + Dreamvisitor.getPlugin().getDescription().getVersion() + "\nDeveloped by Stonley890\nOpen source at https://github.com/Stonley890/Dreamvisitor");
                })
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(CommandPermission.OP)
                        .withHelp("Reload Dreamvisitor.", "Reload Dreamvisitor's config file from disk.")
                        .executes(((sender, args) -> {
                            Dreamvisitor.getPlugin().reloadConfig();
                            sender.sendMessage(Dreamvisitor.PREFIX + "Configuration reloaded.");
                        }))
                )
                .withSubcommand(new CommandAPICommand("manage")
                        .withPermission(CommandPermission.OP)
                        .withHelp("Manage Dreamvisitor config.", "Read from or write to the Dreamvisitor configuration.")
                        .withSubcommands(
                                new CommandAPICommand("debug")
                                        .withHelp("Set debug.", """
                                                Whether to enable debug messages.
                                                This will send additional messages to help debug Dreamvisitor.
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("debug"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "debug";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("website-url")
                                        .withHelp("Set website-url.", """
                                                Website URL
                                                The URL for the whitelisting website.
                                                Used to restrict requests not from the specified website to prevent abuse.
                                                Default: "https://whitelist.woftnw.org\"""")
                                        .withOptionalArguments(new TextArgument("website-url"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "website-url";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("web-whitelist")
                                        .withHelp("Set web-whitelist.", """
                                                Whether web whitelisting is enabled or not
                                                This can be set with the /toggleweb Discord command.
                                                Default: true""")
                                        .withOptionalArguments(new BooleanArgument("web-whitelist"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "web-whitelist";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("chatChannelID")
                                        .withHelp("Set chatChannelID.", """
                                                The channel ID of the game chat.
                                                This can be set on Discord with /setgamechat
                                                Default: 880269118975119410""")
                                        .withOptionalArguments(new LongArgument("chatChannelID"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "chatChannelID";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("logChannelID")
                                        .withHelp("Set logChannelID.", """
                                                The channel ID of the log chat.
                                                This can be set on Discord /setlogchat
                                                Default: 1114730320068104262""")
                                        .withOptionalArguments(new LongArgument("logChannelID"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "logChannelID";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("whitelistChannelID")
                                        .withHelp("Set whitelistChannelID.", """
                                                The channel ID of the whitelist chat.
                                                This can be set on Discord /setwhitelist
                                                Default: 858461513991323688""")
                                        .withOptionalArguments(new LongArgument("whitelistChannelID"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "whitelistChannelID";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("tribe-roles")
                                        .withHelp("Set tribe-roles.", "The role IDs of tribes on the main server.\n" +
                                                "This can be set on Discord with /setrole")
                                        .withArguments(CommandUtils.customTribeArgument("tribe"))
                                        .withOptionalArguments(new LongArgument("role-id"))
                                        .executes((sender, args) -> {
                                            @Nullable Long roleId = (Long) args.get("role-id");
                                            @NotNull Tribe tribe = (Tribe) Objects.requireNonNull(args.get("tribe"));
                                            int tribeIndex = TribeUtil.indexOf(tribe);
                                            List<Long> emblems = plugin.getConfig().getLongList("triberoles");
                                            if (roleId == null) sender.sendMessage(Dreamvisitor.PREFIX + "role of " + tribe.getName() + " is currently set to\n" + emblems.get(tribeIndex));
                                            else {
                                                emblems.set(tribeIndex, roleId);
                                                plugin.getConfig().set("triberoles", emblems);
                                                plugin.saveConfig();
                                                sender.sendMessage(Dreamvisitor.PREFIX + "Set role of " + tribe.getName() + " to\n" + roleId);
                                            }
                                        }),
                                new CommandAPICommand("chatPaused")
                                        .withHelp("Set chatPaused.", """
                                                Whether chat is paused or not.
                                                This can be toggled in Minecraft with /pausechat
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("chatPaused"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "chatPaused";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("softwhitelist")
                                        .withHelp("Set softwhitelist.", """
                                                Whether the soft whitelist is enabled or not
                                                This can be set in Minecraft with /softwhitelist [on|off]
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("softwhitelist"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "softwhitelist";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("playerlimit")
                                        .withHelp("Set playerlimit.", """
                                                Player limit override. This will override the player limit, both over and under.
                                                This can be set in Minecraft with /playerlimit <int>
                                                Default: -1""")
                                        .withOptionalArguments(new IntegerArgument("playerlimit", -1))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "playerlimit";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("disablepvp")
                                        .withHelp("Set disablepvp.", """
                                                Whether to globally disable pvp or not.
                                                This can be toggled in Minecraft with /togglepvp
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("disablepvp"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "disablepvp";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("hubLocation")
                                        .withHelp("Get hubLocation.", """
                                                The location of the recorded hub.
                                                This can be set in Minecraft with /sethub
                                                Default: none""")
                                        .executes((sender, args) -> {
                                            @Nullable String key = "hubLocation";
                                            Location location = plugin.getConfig().getLocation(key);
                                            String reply = "none";
                                            if (location != null) reply = location.toString();
                                            sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + reply);
                                        }),
                                new CommandAPICommand("log-console")
                                        .withHelp("Set log-console.", """
                                                Whether to copy the output of the console to the Discord log channel.
                                                This will disable the default Dreamvisitor logging in place of the Minecraft server console.
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("log-console"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "log-console";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("enable-log-console-commands")
                                        .withHelp("Set enable-log-console-commands.", """
                                                Whether to pass messages in the log channel as console commands.
                                                If log-console is enabled, this will take messages sent by users with the Discord administrator permission and pass
                                                  them as console commands.
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("enable-log-console-commands"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "enable-log-console-commands";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("infraction-expire-time-days")
                                        .withHelp("Set infraction-expire-time-days.", """
                                                The amount of time in days (as an integer) that infractions take to expire.
                                                Expired infractions are not deleted, but they do not count toward a total infraction count.
                                                Default: 90""")
                                        .withOptionalArguments(new IntegerArgument("infraction-expire-time-days", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "infraction-expire-time-days";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("infractions-category-id")
                                        .withHelp("Set infractions-category-id.", """
                                                The ID of the category to create infractions channels.
                                                They will accumulate here.
                                                Default: 1226180189604544593""")
                                        .withOptionalArguments(new LongArgument("infractions-category-id"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "infractions-category-id";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("shopName")
                                        .withHelp("Set shopName.", """
                                                The name of the Discord shop.
                                                This will appear at the top of the embed.
                                                Default: "Shop\"""")
                                        .withOptionalArguments(new TextArgument("shopName"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "shopName";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("currencyIcon")
                                        .withHelp("Set currencyIcon.", """
                                                The icon used for currency in the Discord economy system.
                                                This can be any string, including symbols, letters, emojis, and Discord custom emoji.
                                                Default: "$\"""")
                                        .withOptionalArguments(new TextArgument("currencyIcon"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "currencyIcon";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("dailyBaseAmount")
                                        .withHelp("Set dailyBaseAmount.", """
                                                The base amount given by the /daily Discord command.
                                                This is the default amount before adding the streak bonus. The total amount is decided by dailyBaseAmount + (user's streak * this).
                                                Default: 10.00""")
                                        .withOptionalArguments(new LongArgument("dailyBaseAmount"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "dailyBaseAmount";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("dailyStreakMultiplier")
                                        .withHelp("Set dailyStreakMultiplier.", """
                                                The multiplier of the streak bonus given by the /daily command.
                                                This is multiplied by the streak and added to the base amount. The total amount is decided by dailyBaseAmount + (user's streak * this).
                                                Default: 5.00""")
                                        .withOptionalArguments(new LongArgument("dailyStreakMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "dailyStreakMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("workReward")
                                        .withHelp("Set workReward.", """
                                                The amount gained from the /work command.
                                                /work can only be run every hour.
                                                Default: 20.00""")
                                        .withOptionalArguments(new LongArgument("workReward"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "workReward";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("mailDeliveryLocationSelectionDistanceWeightMultiplier")
                                        .withHelp("Set mailDeliveryLocationSelectionDistanceWeightMultiplier.", """
                                                The multiplier of the distance weight when choosing mail delivery locations.
                                                Takes the ratio (between 0 and 1) of the distance to the maximum distance between locations,
                                                  multiplies it by this value, and adds it to the mail location weight.
                                                This weight is used to randomly choose a mail location to deliver to provide a realistic
                                                  relationship between delivery locations.
                                                At 0, distance has no effect on location selection.
                                                At 1, the weight will have a slight effect on the location selection.
                                                At 10, the weight will have a significant effect on the location selection.
                                                The weight is applied inversely, making closer distances worth more than further distances.
                                                Default: 1.00""")
                                        .withOptionalArguments(new LongArgument("mailDeliveryLocationSelectionDistanceWeightMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "mailDeliveryLocationSelectionDistanceWeightMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("mailDistanceToRewardMultiplier")
                                        .withHelp("Set mailDistanceToRewardMultiplier.", """
                                                Mail delivery reward is calculated by multiplying the distance by this number.
                                                The result is then rounded to the nearest ten.
                                                At 0, the reward given is 0.
                                                At 1, the reward given will be the distance in blocks.
                                                Default: 0.05""")
                                        .withOptionalArguments(new LongArgument("mailDistanceToRewardMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "mailDistanceToRewardMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("currentYear")
                                        .withHelp("Set currentYear.", """
                                                The current in-universe year.
                                                This will be incremented by Dreamvisitor automatically.
                                                Default: 0""")
                                        .withOptionalArguments(new LongArgument("currentYear"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "currentYear";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("minutesPerYear")
                                        .withHelp("Set minutesPerYear.", "The number of minutes in an in-universe year.\n" +
                                                "Default: 43200")
                                        .withOptionalArguments(new LongArgument("minutesPerYear"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "minutesPerYear";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("lastUpdateMilli")
                                        .withHelp("Set lastUpdateMilli.", """
                                                The last time the year was updated.
                                                This is used to keep time accurately, even if the server goes offline.
                                                Do not change this value.
                                                Default: 0""")
                                        .withOptionalArguments(new LongArgument("lastUpdateMilli"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "lastUpdateMilli";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("resourcePackRepo")
                                        .withHelp("Set resourcePackRepo", """
                                                The repository path of the server resource pack.
                                                Dreamvisitor will pull the first artifact from the latest release on pack update.
                                                Default: "WOFTNW/Dragonspeak""")
                                        .withOptionalArguments(new TextArgument("resourcePackRepo"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "resourcePackRepo";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("whitelistPort")
                                        .withHelp("Set whitelistPort.", """
                                                The port to use to accept web whitelist requests.
                                                Default: 10826""")
                                        .withOptionalArguments(new IntegerArgument("whitelistPort", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "whitelistPort";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("flightEnergyCapacity")
                                        .withHelp("Set flightEnergyCapacity.", """
                                                The maximum amount of flight energy.
                                                Default: 400""")
                                        .withOptionalArguments(new IntegerArgument("flightEnergyCapacity", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "flightEnergyCapacity";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("flightRegenerationPoint")
                                        .withHelp("Set flightRegenerationPoint.", """
                                                The point after energy depletion that flight is possible again.
                                                Default: 200.00""")
                                        .withOptionalArguments(new DoubleArgument("flightRegenerationPoint", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "flightRegenerationPoint";
                                            configDouble(sender, args, key);
                                        }),
                                new CommandAPICommand("flightEnergyRegeneration")
                                        .withHelp("Set flightEnergyRegeneration.", """
                                                The rate at which energy is regenerated per tick.
                                                Default: 1.00""")
                                        .withOptionalArguments(new DoubleArgument("flightEnergyRegeneration", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "flightEnergyRegeneration";
                                            configDouble(sender, args, key);
                                        }),
                                new CommandAPICommand("flightEnergyDepletionXYMultiplier")
                                        .withHelp("Set flightEnergyDepletionXYMultiplier.", """
                                                The rate at which movement on the X and Y axes depletes energy while flying.
                                                Default: 4.00""")
                                        .withOptionalArguments(new DoubleArgument("flightEnergyDepletionXYMultiplier", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "flightEnergyDepletionXYMultiplier";
                                            configDouble(sender, args, key);
                                        }),
                                new CommandAPICommand("flightEnergyDepletionYMultiplier")
                                        .withHelp("Set flightEnergyDepletionYMultiplier.", """
                                                The rate at which movement on the Y axes depletes energy while flying.
                                                Default: 10.00""")
                                        .withOptionalArguments(new DoubleArgument("flightEnergyDepletionYMultiplier", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "flightEnergyDepletionYMultiplier";
                                            configDouble(sender, args, key);
                                        }),
                                new CommandAPICommand("noWitherNotice")
                                        .withHelp("Set noWitherNotice", """
                                                The message sent if a Wither is built in a region where the wither flag is denied.
                                                Default: "Withers cannot be spawned here. You can only spawn Withers in the Wither chamber.""")
                                        .withOptionalArguments(new TextArgument("noWitherNotice"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "noWitherNotice";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("daysUntilInactiveTax")
                                        .withHelp("Set daysUntilInactiveTax", """
                                                The days a player can be offline until the inactivity tax applies.
                                                Default: 60""")
                                        .withOptionalArguments(new TextArgument("daysUntilInactiveTax"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "daysUntilInactiveTax";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("inactiveTaxPercent")
                                        .withHelp("Set inactiveTaxPercent", """
                                                The percent to tax inactive players, between 0.0 and 1.0.
                                                Default: 0.1""")
                                        .withOptionalArguments(new TextArgument("inactiveTaxPercent"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "inactiveTaxPercent";
                                            configDouble(sender, args, key);
                                        }),
                                new CommandAPICommand("inactiveDayFrequency")
                                        .withHelp("Set inactiveDayFrequency", """
                                                The time in days between inactivity taxes. 0 to disable.
                                                Default: 7""")
                                        .withOptionalArguments(new TextArgument("inactiveDayFrequency"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "inactiveDayFrequency";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("inactiveTaxStop")
                                        .withHelp("Set inactiveTaxStop", """
                                                The lowest a balance can be depleted to by inactivity taxes.
                                                Default: 50000""")
                                        .withOptionalArguments(new TextArgument("inactiveTaxStop"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "inactiveTaxStop";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("lastInactiveTax")
                                        .withHelp("Set lastInactiveTax", """
                                                The last time the inactive tax occurred. This should not be manually tampered with.
                                                Default: 0""")
                                        .withOptionalArguments(new TextArgument("lastInactiveTax"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "lastInactiveTax";
                                            configInt(sender, args, key);
                                        })
                        )
                );
    }

    private void configBoolean(CommandSender sender, @NotNull CommandArguments args, String key) {
        Boolean value = (Boolean) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getBoolean(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getBoolean(key));
        }
    }

    private void configInt(CommandSender sender, @NotNull CommandArguments args, String key) {
        Integer value = (Integer) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getInt(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getInt(key));
        }
    }

    private void configDouble(CommandSender sender, @NotNull CommandArguments args, String key) {
        Double value = (Double) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getDouble(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getDouble(key));
        }
    }

    private void configLong(CommandSender sender, @NotNull CommandArguments args, String key) {
        Long value = (Long) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getLong(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getLong(key));
        }
    }

    private void configString(CommandSender sender, @NotNull CommandArguments args, String key) {
        String value = (String) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getString(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getString(key));
        }
    }

    @SuppressWarnings("unchecked")
    private void configLongList(CommandSender sender, @NotNull CommandArguments args, String key) {
        List<Long> value = (List<Long>) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getLongList(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getLongList(key));
        }
    }
}
