package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import io.github.stonley890.dreamvisitor.functions.Messager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DCmdEconomy extends ListenerAdapter implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("economy", "Manage the Discord economy.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .addSubcommandGroups(
                        new SubcommandGroupData("shop", "Manage the shop.").addSubcommands(
                                new SubcommandData("name","Get or set the name of the shop.")
                                        .addOption(OptionType.STRING, "new-name","The name to set.", false, false),
                                new SubcommandData("currency-symbol", "Get or set the currency symbol.")
                                        .addOption(OptionType.STRING, "new-symbol", "The symbol to set.", false, false)
                        ),
                        new SubcommandGroupData("items", "Manage items.").addSubcommands(
                                new SubcommandData("list", "List all items."),
                                new SubcommandData("edit", "Edit an item.")
                                        .addOption(OptionType.INTEGER, "id", "The ID of the item to edit.", true),
                                new SubcommandData("add", "Create a new item.")
                                        .addOption(OptionType.STRING, "name", "The name of the item.", true)
                                        .addOption(OptionType.STRING, "description", "The description of this item.", true),
                                new SubcommandData("remove", "Permanently remove an item.")
                                        .addOption(OptionType.STRING, "id", "The ID of the item to remove.", true)
                        ),
                        new SubcommandGroupData("users", "Manage users.").addSubcommands(
                                new SubcommandData("balance", "Get or set the balance of a user.")
                                        .addOption(OptionType.USER, "user", "The user whose balance to get.", true)
                                        .addOption(OptionType.NUMBER, "new-balance", "The balance to set.", false),
                                new SubcommandData("get-items", "Manage the items of a user.")
                                        .addOption(OptionType.USER, "user", "The user whose items to get.", true),
                                new SubcommandData("set-items", "Set the quantity of an item held by a user.")
                                        .addOption(OptionType.USER, "user", "The user whose items to get.", true)
                                        .addOption(OptionType.INTEGER, "id", "The ID of the item to modify the quantity of.", true)
                                        .addOption(OptionType.INTEGER, "new-quantity", "The number of this item the user should have.", true),
                                new SubcommandData("daily-streak", "Get or set the daily streak of a user.")
                                        .addOption(OptionType.USER, "user", "The user whose streak to get.", true)
                                        .addOption(OptionType.INTEGER, "new-value", "The streak to set for this player.", false)
                        )
                );
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String subcommandGroup = event.getSubcommandGroup();
        String subcommand = event.getSubcommandName();

        if (subcommandGroup == null || subcommand == null) {
            event.reply("Subcommand Group or Subcommand is null.").setEphemeral(true).queue();
            return;
        }

        switch (subcommandGroup) {
            case "shop" -> {
                if (subcommand.equals("name")) {
                    String name = event.getOption("new-name", OptionMapping::getAsString);
                    if (name == null) {
                        event.reply("The current shop name is " + Economy.getShopName()).setEphemeral(true).queue();
                    } else {
                        String shopName = Economy.getShopName();
                        Economy.setShopName(name);
                        event.reply("Changed shop name from " + shopName + " to " + name + ".").queue();
                    }
                } else if (subcommand.equals("currency-symbol")) {
                    String symbol = event.getOption("new-symbol", OptionMapping::getAsString);
                    if (symbol == null) {
                        event.reply("The current shop symbol is " + Economy.getCurrencySymbol()).setEphemeral(true).queue();
                    } else {
                        String oldSymbol = Economy.getCurrencySymbol();
                        Economy.setCurrencySymbol(symbol);
                        event.reply("Changed currency symbol from " + oldSymbol + " to " + symbol + ".").queue();
                    }
                } else {
                    event.reply("Subcommand not found.").setEphemeral(true).queue();
                }
            }
            case "items" -> {
                switch (subcommand) {
                    case "list" -> {

                        EmbedBuilder embed = new EmbedBuilder();

                        List<Economy.ShopItem> items = Economy.getItems();
                        if (items.isEmpty()) {
                            embed.setDescription("There are currently no items.");
                            event.replyEmbeds(embed.build()).queue();
                            return;
                        }

                        for (int j = 0; j < items.size(); j++) {
                            Economy.ShopItem item = items.get(j);

                            StringBuilder description = new StringBuilder();

                            description.append(item.getDescription()).append("\n\n")
                                    .append("Price: ").append(Economy.getCurrencySymbol()).append(item.getPrice())
                                    .append("\nSale Percent: ").append(item.getSalePercent()).append("%")
                                    .append("\nQuantity: ");
                            if (item.isInfinite()) description.append("Infinite");
                            else description.append(item.getQuantity());
                            description.append("\nEnabled: ").append(item.isEnabled());
                            description.append("\nGifting: ").append(item.isGiftingEnabled());
                            description.append("\nUsable: ").append(!item.isUseDisabled());
                            description.append("\nUse on Purchase: ").append(item.isUseOnPurchase());
                            description.append("\nMax Allowed: ");
                            if (item.getMaxAllowed() == -1) description.append("Infinite");
                            else description.append(item.getMaxAllowed());

                            description.append("\nRoles Add on Use: ");
                            List<Long> onUseRolesAdd = item.getOnUseRolesAdd();
                            if (onUseRolesAdd == null || onUseRolesAdd.isEmpty()) description.append("None");
                            else {
                                for (int i = 0; i < onUseRolesAdd.size(); i++) {
                                    Long roleId = onUseRolesAdd.get(i);
                                    Role role = Objects.requireNonNull(event.getGuild()).getRoleById(roleId);
                                    if (role == null) {
                                        item.getOnUseRolesAdd().remove(roleId);
                                        items.set(i, item);
                                        continue;
                                    }
                                    description.append(role.getAsMention());
                                    if (i == onUseRolesAdd.size() - 1) description.append(", ");
                                }
                                description.delete(description.length(), description.length());
                            }

                            description.append("\nRoles Remove on Use: ");
                            List<Long> onUseRolesRemove = item.getOnUseRolesRemove();
                            if (onUseRolesRemove == null || onUseRolesRemove.isEmpty()) description.append("None");
                            else {
                                for (int i = 0; i < onUseRolesRemove.size(); i++) {
                                    Long roleId = onUseRolesRemove.get(i);
                                    Role role = Objects.requireNonNull(event.getGuild()).getRoleById(roleId);
                                    if (role == null) {
                                        item.getOnUseRolesRemove().remove(roleId);
                                        items.set(i, item);
                                        continue;
                                    }
                                    description.append(role.getAsMention());
                                    if (i == onUseRolesRemove.size() - 1) description.append(", ");
                                }
                                description.delete(description.length(), description.length());
                            }

                            description.append("\nGroups Parent on Use: ");
                            List<String> onUseGroupsAdd = item.getOnUseGroupsAdd();
                            if (onUseGroupsAdd == null || onUseGroupsAdd.isEmpty()) description.append("None");
                            else {
                                for (int i = 0; i < onUseGroupsAdd.size(); i++) {
                                    String groupName = onUseGroupsAdd.get(i);
                                    description.append(groupName);
                                    if (i == onUseGroupsAdd.size() - 1) description.append(", ");
                                }
                                description.delete(description.length(), description.length());
                            }

                            description.append("\nGroups Unparent on Use: ");
                            List<String> onUseGroupsRemove = item.getOnUseGroupsRemove();
                            if (onUseGroupsRemove == null || onUseGroupsRemove.isEmpty()) description.append("None");
                            else {
                                for (int i = 0; i < onUseGroupsRemove.size(); i++) {
                                    String groupName = onUseGroupsRemove.get(i);
                                    description.append(groupName);
                                    if (i == onUseGroupsRemove.size() - 1) description.append(", ");
                                }
                                description.delete(description.length(), description.length());
                            }

                            description.append("\nCommands on Use: ");
                            List<String> onUseConsoleCommands = item.getOnUseConsoleCommands();
                            if (onUseConsoleCommands == null || onUseConsoleCommands.isEmpty())
                                description.append("None");
                            else {
                                for (int i = 0; i < onUseConsoleCommands.size(); i++) {
                                    String groupName = onUseConsoleCommands.get(i);
                                    description.append(groupName);
                                    if (i == onUseConsoleCommands.size() - 1) description.append(", ");
                                }
                                description.delete(description.length(), description.length());
                            }

                            embed.addField(item.getName() + " - `" + item.getId() + "`", description.toString(), false);
                        }

                        Economy.saveItems(items);

                        event.replyEmbeds(embed.build()).queue();

                    }
                    case "edit" -> {

                        int itemId;
                        try {
                            itemId = Objects.requireNonNull(event.getOption("id", OptionMapping::getAsInt));
                        } catch (NullPointerException e) {
                            event.reply("ID cannot be null!").setEphemeral(true).queue();
                            return;
                        }
                        Economy.ShopItem item = Economy.getItem(itemId);
                        if (item == null) {
                            event.reply("That item does not exist.").setEphemeral(true).queue();
                            return;
                        }

                        EmbedBuilder embed = getEditEmbed(item, Objects.requireNonNull(event.getGuild()));
                        event.replyEmbeds(embed.build()).setEphemeral(true).addActionRow(getEditDropdown(itemId).build()).queue();

                    }
                    case "add" -> {

                        Messager.debug("Call to add.");

                        String name = event.getOption("name", OptionMapping::getAsString);
                        if (name == null) {
                            event.reply("name cannot be null!").setEphemeral(true).queue();
                            return;
                        }
                        String description = event.getOption("description", OptionMapping::getAsString);
                        if (description == null) {
                            event.reply("description cannot be null!").setEphemeral(true).queue();
                            return;
                        }
                        Messager.debug("Got args.");

                        if (Economy.getItems().size() >= 25) {
                            event.reply("Only 25 items can exist at a time!").setEphemeral(true).queue();
                            return;
                        }
                        Economy.ShopItem shopItem = new Economy.ShopItem(name, description);
                        Messager.debug("Created new item.");
                        shopItem.ensureUniqueId();
                        Messager.debug("ID is unique.");
                        int itemId = shopItem.getId();
                        shopItem.setEnabled(false);
                        Messager.debug("Disabled.");
                        Economy.saveItem(shopItem);
                        Messager.debug("Saves.");

                        EmbedBuilder embed = getEditEmbed(shopItem, Objects.requireNonNull(event.getGuild()));
                        Messager.debug("Got embed.");

                        event.reply("The item has been created, but not enabled. Edit it below and set it to enabled when ready.")
                                .addEmbeds(embed.build()).setEphemeral(true).addActionRow(getEditDropdown(itemId).build()).queue();
                        Messager.debug("Reply queued..");

                    }
                    case "remove" -> {

                        int itemId;
                        try {
                            itemId = Objects.requireNonNull(event.getOption("id", OptionMapping::getAsInt));
                        } catch (NullPointerException e) {
                            event.reply("ID cannot be null!").setEphemeral(true).queue();
                            return;
                        }

                        Economy.ShopItem item = Economy.getItem(itemId);
                        if (item == null) {
                            event.reply("That item could not be found.").setEphemeral(true).queue();
                            return;
                        }

                        List<Economy.ShopItem> items = Economy.getItems();

                        Button confirm = Button.danger("item-" + itemId + "-delete", "Yes, permanently delete.");

                        StringBuilder description = new StringBuilder();

                        description.append(item.getDescription()).append("\n\n")
                                .append("Price: ").append(Economy.getCurrencySymbol()).append(item.getPrice())
                                .append("\nSale Percent: ").append(item.getSalePercent()).append("%")
                                .append("\nQuantity: ");
                        if (item.isInfinite()) description.append("Infinite");
                        else description.append(item.getQuantity());
                        description.append("\nEnabled: ").append(item.isEnabled());
                        description.append("\nGifting: ").append(item.isGiftingEnabled());
                        description.append("\nUsable: ").append(!item.isUseDisabled());
                        description.append("\nMax Allowed: ");
                        if (item.getMaxAllowed() == -1) description.append("Infinite");
                        else description.append(item.getMaxAllowed());

                        description.append("\nRoles Add on Use: ");
                        List<Long> onUseRolesAdd = item.getOnUseRolesAdd();
                        if (onUseRolesAdd == null || onUseRolesAdd.isEmpty()) description.append("None");
                        else {
                            for (int i = 0; i < onUseRolesAdd.size(); i++) {
                                Long roleId = onUseRolesAdd.get(i);
                                Role role = Objects.requireNonNull(event.getGuild()).getRoleById(roleId);
                                if (role == null) {
                                    item.getOnUseRolesAdd().remove(roleId);
                                    items.set(i, item);
                                    continue;
                                }
                                description.append(role.getAsMention());
                                if (i == onUseRolesAdd.size() - 1) description.append(", ");
                            }
                            description.delete(description.length(), description.length());
                        }

                        description.append("\nRoles Remove on Use: ");
                        List<Long> onUseRolesRemove = item.getOnUseRolesRemove();
                        if (onUseRolesRemove == null || onUseRolesRemove.isEmpty()) description.append("None");
                        else {
                            for (int i = 0; i < onUseRolesRemove.size(); i++) {
                                Long roleId = onUseRolesRemove.get(i);
                                Role role = Objects.requireNonNull(event.getGuild()).getRoleById(roleId);
                                if (role == null) {
                                    item.getOnUseRolesRemove().remove(roleId);
                                    items.set(i, item);
                                    continue;
                                }
                                description.append(role.getAsMention());
                                if (i == onUseRolesRemove.size() - 1) description.append(", ");
                            }
                            description.delete(description.length(), description.length());
                        }

                        description.append("\nGroups Parent on Use: ");
                        List<String> onUseGroupsAdd = item.getOnUseGroupsAdd();
                        if (onUseGroupsAdd == null || onUseGroupsAdd.isEmpty()) description.append("None");
                        else {
                            for (int i = 0; i < onUseGroupsAdd.size(); i++) {
                                String groupName = onUseGroupsAdd.get(i);
                                description.append(groupName);
                                if (i == onUseGroupsAdd.size() - 1) description.append(", ");
                            }
                            description.delete(description.length(), description.length());
                        }

                        description.append("\nGroups Unparent on Use: ");
                        List<String> onUseGroupsRemove = item.getOnUseGroupsRemove();
                        if (onUseGroupsRemove == null || onUseGroupsRemove.isEmpty()) description.append("None");
                        else {
                            for (int i = 0; i < onUseGroupsRemove.size(); i++) {
                                String groupName = onUseGroupsRemove.get(i);
                                description.append(groupName);
                                if (i == onUseGroupsRemove.size() - 1) description.append(", ");
                            }
                            description.delete(description.length(), description.length());
                        }

                        description.append("\nCommands on Use: ");
                        List<String> onUseConsoleCommands = item.getOnUseConsoleCommands();
                        if (onUseConsoleCommands == null || onUseConsoleCommands.isEmpty()) description.append("None");
                        else {
                            for (int i = 0; i < onUseConsoleCommands.size(); i++) {
                                String groupName = onUseConsoleCommands.get(i);
                                description.append(groupName);
                                if (i == onUseConsoleCommands.size() - 1) description.append(", ");
                            }
                            description.delete(description.length(), description.length());
                        }

                        Economy.saveItems(items);

                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Are you sure you want to delete this item?").setDescription(description)
                                .setFooter("If you delete this item, it will be permanently removed from the shop and be cleared from all users. If you instead just want to disable this item, use /economy items edit This action cannot be undone.");
                        event.replyEmbeds(embed.build()).addActionRow(confirm).setEphemeral(true).queue();

                    }
                }
            }
            case "users" -> {

                switch (subcommand) {
                    case "balance" -> {
                        User user = event.getOption("user", OptionMapping::getAsUser);
                        if (user == null) {
                            event.reply("That user could not be found.").setEphemeral(true).queue();
                            return;
                        }

                        Economy.Consumer consumer = Economy.getConsumer(user.getIdLong());
                        double balance = consumer.getBalance();

                        try {
                            double newBalance = event.getOption("new-balance", OptionMapping::getAsDouble);
                            consumer.setBalance(newBalance);
                            Economy.saveConsumer(consumer);

                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setAuthor(user.getName(), null, user.getAvatarUrl());
                            embed.setDescription("Changed " + user.getAsMention() + "'s balance from " + Economy.getCurrencySymbol() + balance + " to " + Economy.getCurrencySymbol() + newBalance);

                            event.replyEmbeds(embed.build()).queue();
                        } catch (NullPointerException e) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setAuthor(user.getName(), null, user.getAvatarUrl());
                            embed.setDescription(user.getAsMention() + "'s current balance is " + Economy.getCurrencySymbol() + balance);

                            event.replyEmbeds(embed.build()).queue();
                        }
                    }
                    case "get-items" -> {
                        User user = event.getOption("user", OptionMapping::getAsUser);
                        if (user == null) {
                            event.reply("That user could not be found.").setEphemeral(true).queue();
                            return;
                        }

                        Economy.Consumer consumer = Economy.getConsumer(user.getIdLong());

                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setAuthor(user.getName(), null, user.getAvatarUrl());
                        embed.setTitle(user.getEffectiveName() + "'s Inventory");

                        Map<Integer, Integer> ownedItems = consumer.getItems();
                        List<Economy.ShopItem> items = Economy.getItems();

                        for (Economy.ShopItem item : items) {
                            int id = item.getId();
                            Integer quantityOwned = ownedItems.get(id);
                            if (quantityOwned == null || quantityOwned == 0) {
                                continue;
                            }
                            embed.addField(quantityOwned + " " + item.getName(), "`" + id + "`", true);
                        }

                        event.replyEmbeds(embed.build()).queue();
                    }
                    case "set-items" -> {
                        User user = event.getOption("user", OptionMapping::getAsUser);
                        Integer id = event.getOption("id", OptionMapping::getAsInt);
                        Integer quantity = event.getOption("new-quantity", OptionMapping::getAsInt);
                        assert id != null;
                        assert quantity != null;

                        Economy.ShopItem item = Economy.getItem(id);
                        if (item == null) {
                            event.reply("That item does not exist!").setEphemeral(true).queue();
                            return;
                        }

                        if (quantity < 0) {
                            event.reply("Quantity must be a positive number!").setEphemeral(true).queue();
                            return;
                        }
                        if (quantity > item.getMaxAllowed() && item.getMaxAllowed() != -1) {
                            event.reply("You cannot set the quantity to above the max allowed!").setEphemeral(true).queue();
                            return;
                        }

                        if (user == null) {
                            event.reply("That user could not be found.").setEphemeral(true).queue();
                            return;
                        }

                        Economy.Consumer consumer = Economy.getConsumer(user.getIdLong());
                        consumer.setItemQuantity(id, quantity);
                        Economy.saveConsumer(consumer);

                        event.reply("Set quantity of " + item.getName() + " owned by " + user.getAsMention() + " to " + quantity + ".").queue();
                    } case "daily-streak" -> {
                        User user = event.getOption("user", OptionMapping::getAsUser);
                        Integer value = event.getOption("new-value", OptionMapping::getAsInt);

                        if (user == null) {
                            event.reply("User cannot be null!").setEphemeral(true).queue();
                            return;
                        }

                        Economy.Consumer consumer = Economy.getConsumer(user.getIdLong());

                        if (value == null) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setDescription(user.getAsMention() + " has a daily streak of " + consumer.getGameData().getDailyStreak());

                            event.replyEmbeds(embed.build()).queue();
                            return;
                        }

                        Economy.GameData gameData = consumer.getGameData();
                        gameData.setDailyStreak(value);
                        gameData.setLastDaily(LocalDateTime.now().minusHours(24));
                        consumer.setGameData(gameData);
                        Economy.saveConsumer(consumer);

                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setDescription(user.getAsMention() + "'s streak has been set to " + value + " and their cooldown has been reset.");

                        event.replyEmbeds(embed.build()).queue();
                    }
                }
            }
            default -> event.reply("Subcommand group not found.").setEphemeral(true).queue();
        }

    }

    @NotNull
    private static EmbedBuilder getEditEmbed(@NotNull Economy.ShopItem item, @NotNull Guild guild) {

        EmbedBuilder embed = new EmbedBuilder();

        StringBuilder description = new StringBuilder();

        description.append(item.getDescription()).append("\n\n")
                .append("Price: ").append(Economy.getCurrencySymbol()).append(item.getPrice())
                .append("\nSale Percent: ").append(item.getSalePercent()).append("%")
                .append("\nQuantity: ");
        if (item.isInfinite()) description.append("Infinite");
        else description.append(item.getQuantity());
        description.append("\nEnabled: ").append(item.isEnabled());
        description.append("\nGifting: ").append(item.isGiftingEnabled());
        description.append("\nUsable: ").append(!item.isUseDisabled());
        description.append("\nMax Allowed: ");
        if (item.getMaxAllowed() == -1) description.append("Infinite");
        else description.append(item.getMaxAllowed());

        description.append("\nRoles Add on Use: ");
        List<Long> onUseRolesAdd = item.getOnUseRolesAdd();
        if (onUseRolesAdd == null || onUseRolesAdd.isEmpty()) description.append("None");
        else {
            for (int i = 0; i < onUseRolesAdd.size(); i++) {
                Long roleId = onUseRolesAdd.get(i);
                Role role = guild.getRoleById(roleId);
                if (role == null) {
                    item.getOnUseRolesAdd().remove(roleId);
                    Economy.saveItem(item);
                    continue;
                }
                description.append(role.getAsMention());
                if (i == onUseRolesAdd.size() - 1) description.append(", ");
            }
            description.delete(description.length(), description.length());
        }

        description.append("\nRoles Remove on Use: ");
        List<Long> onUseRolesRemove = item.getOnUseRolesRemove();
        if (onUseRolesRemove == null || onUseRolesRemove.isEmpty()) description.append("None");
        else {
            for (int i = 0; i < onUseRolesRemove.size(); i++) {
                Long roleId = onUseRolesRemove.get(i);
                Role role = guild.getRoleById(roleId);
                if (role == null) {
                    item.getOnUseRolesRemove().remove(roleId);
                    Economy.saveItem(item);
                    continue;
                }
                description.append(role.getAsMention());
                if (i == onUseRolesRemove.size() - 1) description.append(", ");
            }
            description.delete(description.length(), description.length());
        }

        description.append("\nGroups Parent on Use: ");
        List<String> onUseGroupsAdd = item.getOnUseGroupsAdd();
        if (onUseGroupsAdd == null || onUseGroupsAdd.isEmpty()) description.append("None");
        else {
            for (int i = 0; i < onUseGroupsAdd.size(); i++) {
                String groupName = onUseGroupsAdd.get(i);
                description.append(groupName);
                if (i == onUseGroupsAdd.size() - 1) description.append(", ");
            }
            description.delete(description.length(), description.length());
        }

        description.append("\nGroups Unparent on Use: ");
        List<String> onUseGroupsRemove = item.getOnUseGroupsRemove();
        if (onUseGroupsRemove == null || onUseGroupsRemove.isEmpty()) description.append("None");
        else {
            for (int i = 0; i < onUseGroupsRemove.size(); i++) {
                String groupName = onUseGroupsRemove.get(i);
                description.append(groupName);
                if (i == onUseGroupsRemove.size() - 1) description.append(", ");
            }
            description.delete(description.length(), description.length());
        }

        description.append("\nCommands on Use: ");
        List<String> onUseConsoleCommands = item.getOnUseConsoleCommands();
        if (onUseConsoleCommands == null || onUseConsoleCommands.isEmpty()) description.append("None");
        else {
            for (int i = 0; i < onUseConsoleCommands.size(); i++) {
                String groupName = onUseConsoleCommands.get(i);
                description.append(groupName);
                if (i == onUseConsoleCommands.size() - 1) description.append(", ");
            }
            description.delete(description.length(), description.length());
        }

        embed.setTitle(item.getName() + " - " + item.getId());
        embed.setDescription(description.toString());
        embed.setFooter("Choose a property from the select menu to edit.");

        return embed;
    }

    @NotNull
    private static StringSelectMenu.Builder getEditDropdown(int itemId) {

        StringSelectMenu.Builder selectMenu = StringSelectMenu.create("item-" + itemId + "-edit-select");
        return selectMenu
                .addOption("Name", "name", "The name of the item.")
                .addOption("Description", "description", "The description of the item.")
                .addOption("Price", "price", "The regular price of the item.")
                .addOption("Sale Percent", "salePercent", "The percent to remove from the regular price.")
                .addOption("Quantity", "quantity", "The quantity of this item available in the shop.")
                .addOption("Max Allowed", "maxAllowed", "The maximum quantity of this item a user can have at a time.")
                .addOption("Enabled", "enabled", "Whether the item can be bought. Disabled items can still be used.")
                .addOption("Gifting Enabled", "giftingEnabled", "Where the item can be transferred to another user.")
                .addOption("Use Disabled", "useDisabled", "Whether the item's use is disabled. Useful for vanity items.")
                .addOption("Use On Purchase", "useOnPurchase", "Whether the item is auto-used upon purchase.")
                .addOption("Roles to Add", "onUseRolesAdd", "The roles to add upon use.")
                .addOption("Roles to Remove", "onUseRolesRemove", "The roles to remove upon use.")
                .addOption("Groups to Add", "onUseGroupsAdd", "The permission groups to add upon use.")
                .addOption("Groups to Remove", "onUseGroupsRemove", "The permission groups to remove upon use.")
                .addOption("Console Commands", "onUseConsoleCommands", "The commands to execute upon use.");

    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        String[] splitId = id.split("-");

        Messager.debug("Got string select menu with id " + id);

        if (event.getSelectedOptions().isEmpty()) return;
        SelectOption selectedOption = event.getSelectedOptions().get(0);

        String type = splitId[0];
        if (type.equals("item")) {

            int itemId = Integer.parseInt(splitId[1]);
            Economy.ShopItem item = Economy.getItem(itemId);
            if (item == null) {
                event.reply("That item could not be found.").setEphemeral(true).queue();
                return;
            }

            String action = splitId[2];
            if (action.equals("edit")) {
                String subAction = splitId[3];
                if (subAction.equals("select")) {
                    String value = selectedOption.getValue();

                    if (value.equals("enabled") || value.equals("giftingEnabled") || value.equals("useDisabled") || value.equals("useOnPurchase")) {
                        StringSelectMenu.Builder toggleMenu = StringSelectMenu.create("item-" + itemId + "-edit-toggle-" + value);
                        toggleMenu.addOption("True", "true").addOption("False", "false");
                        toggleMenu.setPlaceholder("Set to true or false");

                        event.editComponents(event.getMessage().getActionRows().get(0)).queue(); // remove all action rows except value select
                        List<LayoutComponent> components = new ArrayList<>(event.getMessage().getComponents());
                        components.add(ActionRow.of(toggleMenu.build()));
                        event.getHook().editOriginalComponents(components).queue();
                    } else if (value.equals("onUseRolesAdd") || value.equals("onUseRolesRemove")) {

                        EntitySelectMenu.Builder selectMenu;

                        if (value.equals("onUseRolesAdd")) {
                            selectMenu = EntitySelectMenu.create("item-" + itemId + "-edit-roles-add", EntitySelectMenu.SelectTarget.ROLE);
                            if (item.getOnUseRolesAdd() != null) {
                                List<EntitySelectMenu.DefaultValue> existingValues = new ArrayList<>();
                                for (Long l : item.getOnUseRolesAdd()) {
                                    existingValues.add(EntitySelectMenu.DefaultValue.role(l));
                                }
                                selectMenu.setDefaultValues(existingValues);
                            }
                        } else {
                            selectMenu = EntitySelectMenu.create("item-" + itemId + "-edit-roles-remove", EntitySelectMenu.SelectTarget.ROLE);
                            if (item.getOnUseRolesRemove() != null) {
                                List<EntitySelectMenu.DefaultValue> existingValues = new ArrayList<>();
                                for (Long l : item.getOnUseRolesRemove()) {
                                    existingValues.add(EntitySelectMenu.DefaultValue.role(l));
                                }
                                selectMenu.setDefaultValues(existingValues);
                            }
                        }

                        selectMenu.setPlaceholder("Select roles to include");

                        event.editComponents(event.getMessage().getActionRows().get(0)).queue(); // remove all action rows except value select
                        List<LayoutComponent> components = new ArrayList<>(event.getMessage().getComponents());
                        components.add(ActionRow.of(selectMenu.build()));
                        event.getHook().editOriginalComponents(components).queue();


                    } else {

                        Modal.Builder modal = Modal.create("item-" + itemId + "-edit-" + value, "Change " + value + " of item " + itemId);
                        modal.addActionRow(TextInput.create("new" + value, "New " + value, TextInputStyle.PARAGRAPH).build());
                        event.replyModal(modal.build()).queue();
                        event.getHook().editOriginalComponents(event.getMessage().getActionRows().get(0)).queue(); // remove all action rows except value select
                    }
                } else if (subAction.equals("toggle")) {
                    String value = splitId[4];
                    boolean bool = Boolean.parseBoolean(selectedOption.getValue());
                    switch (value) {
                        case "enabled" -> item.setEnabled(bool);
                        case "giftingEnabled" -> item.setGiftingEnabled(bool);
                        case "useDisabled" -> item.setUseDisabled(bool);
                        case "useOnPurchase" -> item.setUseOnPurchase(bool);
                        default -> {
                            event.reply("Did not expect this property to be set this way!").setEphemeral(true).queue();
                            return;
                        }
                    }
                    Economy.saveItem(item);
                    event.editMessageEmbeds(getEditEmbed(item, Objects.requireNonNull(event.getGuild())).build()).queue();
                    Objects.requireNonNull(event.getMessage()).reply("Toggled " + value + " of item `" + item.getId() + "` to " + bool + ".").queue();
                } else {
                    event.reply("Unexpected request: " + id).queue();
                }
            } else {
                event.reply("Unexpected request: " + id).queue();
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        String id = event.getComponentId();
        String[] splitId = id.split("-");

        Messager.debug("Got string select menu with id " + id);

        String type = splitId[0];
        if (type.equals("item")) {

            int itemId = Integer.parseInt(splitId[1]);
            Economy.ShopItem item = Economy.getItem(itemId);
            if (item == null) {
                event.reply("That item could not be found.").setEphemeral(true).queue();
                return;
            }

            String action = splitId[2];
            if (action.equals("edit")) {
                String subAction = splitId[3];
                if (subAction.equals("roles")) {
                    String roleAction = splitId[4];
                    if (roleAction.equals("add")) {

                        List<Long> onUseRolesAdd = item.getOnUseRolesAdd();
                        if (onUseRolesAdd == null) onUseRolesAdd = new ArrayList<>();

                        for (Role role : event.getMentions().getRoles()) {
                            onUseRolesAdd.add(role.getIdLong());
                        }

                        item.setOnUseRolesAdd(onUseRolesAdd);
                        Economy.saveItem(item);
                        event.editMessageEmbeds(getEditEmbed(item, Objects.requireNonNull(event.getGuild())).build()).queue();
                        Objects.requireNonNull(event.getMessage()).reply("Set onUseRolesAdd of item `" + item.getId() + "` to " + event.getMentions().getRoles().size() + " role(s).").queue();

                    } else if (roleAction.equals("remove")) {

                        List<Long> onUseRolesRemove = item.getOnUseRolesAdd();
                        if (onUseRolesRemove == null) onUseRolesRemove = new ArrayList<>();

                        for (Role role : event.getMentions().getRoles()) {
                            onUseRolesRemove.add(role.getIdLong());
                        }

                        item.setOnUseRolesRemove(onUseRolesRemove);
                        Economy.saveItem(item);
                        event.editMessageEmbeds(getEditEmbed(item, Objects.requireNonNull(event.getGuild())).build()).queue();
                        Objects.requireNonNull(event.getMessage()).reply("Set onUseRolesRemove of item `" + item.getId() + "` to " + event.getMentions().getRoles().size() + " role(s).").queue();

                    } else {
                        event.reply("Unexpected request: " + id).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        assert id != null;
        String[] splitId = id.split("-");

        Messager.debug("Got button with id " + id);

        String type = splitId[0];
        if (type.equals("item")) {

            int itemId = Integer.parseInt(splitId[1]);
            Economy.ShopItem item = Economy.getItem(itemId);
            if (item == null) {
                event.reply("That item could not be found.").setEphemeral(true).queue();
                return;
            }

            String action = splitId[2];
            if (action.equals("delete")) {
                Economy.removeItem(item);
                event.editMessage("Deleted item `" + item.getId() + "`.").queue();
                event.getHook().editOriginalEmbeds(new ArrayList<>()).queue();
                event.editButton(null).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String id = event.getModalId();
        String[] splitId = id.split("-");

        Messager.debug("Got modal with id " + id);

        String type = splitId[0];
        if (type.equals("item")) {
            int itemId = Integer.parseInt(splitId[1]);
            Economy.ShopItem item = Economy.getItem(itemId);

            if (item == null) {
                event.reply("That item does not exist!").setEphemeral(true).queue();
                return;
            }

            String action = splitId[2];
            if (action.equals("edit")) {

                String editValue = splitId[3];
                String modalResponse = event.getValues().get(0).getAsString();

                try {
                    switch (editValue) {
                        case "name" -> item.setName(modalResponse);
                        case "description" -> item.setDescription(modalResponse);
                        case "price" -> item.setPrice(Double.parseDouble(modalResponse));
                        case "salePercent" -> item.setSalePercent(Double.parseDouble(modalResponse));
                        case "quantity" -> item.setQuantity(Integer.parseInt(modalResponse));
                        case "maxAllowed" -> item.setMaxAllowed(Integer.parseInt(modalResponse));
                        case "onUseGroupsAdd" -> {
                            List<String> groups = new ArrayList<>();
                            for (String s : modalResponse.split(",")) groups.add(s.strip());
                            item.setOnUseGroupsAdd(groups);
                        }
                        case "onUseGroupsRemove" -> {
                            List<String> groups = new ArrayList<>();
                            for (String s : modalResponse.split(",")) groups.add(s.strip());
                            item.setOnUseGroupsRemove(groups);
                        }
                        case "onUseConsoleCommands" -> {
                            List<String> commands = new ArrayList<>();
                            for (String s : modalResponse.split(",")) commands.add(s.strip());
                            item.setOnUseConsoleCommands(commands);
                        }
                        default -> {
                            event.reply("Did not expect this property to be set this way!").setEphemeral(true).queue();
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    event.reply("Could not parse response as a number!").setEphemeral(true).queue();
                    return;
                }

                Economy.saveItem(item);
                event.editMessageEmbeds(getEditEmbed(item, Objects.requireNonNull(event.getGuild())).build()).queue();
                Objects.requireNonNull(event.getMessage()).reply("Changed " + editValue + " of item `" + itemId + "` to " + modalResponse).queue();
            }
        }
    }
}
