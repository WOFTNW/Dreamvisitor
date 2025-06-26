package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import io.github.stonley890.dreamvisitor.functions.Messager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DCmdInventory extends ListenerAdapter implements DiscordCommand {

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("inventory", "View your inventory.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());
        Map<Integer, Integer> items = consumer.getItems();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Your Inventory");

        if (items.isEmpty()) {
            embed.setDescription("You do not currently have any items. You can purchase items from the shop.");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        embed = getInvEmbed(consumer);

        if (embed.getFields().isEmpty()) {
            embed.setDescription("You do not currently have any items. You can purchase items from the shop.");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        Button useItem = Button.primary("inv-" + consumer.getId() + "-use", "Use an Item");
        Button giftItem = Button.success("inv-" + consumer.getId() + "-gift", "Gift an Item");

        event.replyEmbeds(embed.build()).addActionRow(useItem, giftItem).queue();

    }

    @NotNull
    private static EmbedBuilder getInvEmbed(Economy.Consumer consumer) {

        EmbedBuilder embed = new EmbedBuilder();

        List<Economy.ShopItem> shopItems = Economy.getItems();
        for (Economy.ShopItem shopItem : shopItems) {
            int quantityOfItem = consumer.getQuantityOfItem(shopItem.getId());
            if (quantityOfItem == 0) continue;
            String useNotice = "This item can be used.";
            String giftNotice = "This item can be gifted.";
            if (shopItem.isUseDisabled()) useNotice = "This item cannot be used.";
            if (!shopItem.isGiftingEnabled()) giftNotice = "This item cannot be gifted.";
            embed.addField("**" + quantityOfItem + "** " + shopItem.getName(), useNotice + "\n" + giftNotice, true);
        }

        return embed;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if (id == null) return;

        String[] split = id.split("-");

        if (!id.startsWith("inv-")) return;

        long consumerId = Long.parseLong(split[1]);

        if (consumerId != event.getUser().getIdLong()) {
            event.reply("Only the invoker of this command can interact with this.").setEphemeral(true).queue();
            return;
        }

        Economy.Consumer consumer = Economy.getConsumer(consumerId);
        if (split[2].equals("use")) {
            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("inv-" + consumerId + "-use");
            selectMenu.setPlaceholder("Select an item to use");
            for (Economy.ShopItem item : Economy.getItems()) {
                if (consumer.getItemQuantity(item.getId()) > 0 && !item.isUseDisabled()) {
                    selectMenu.addOption(item.getName(), String.valueOf(item.getId()));
                }
            }
            if (selectMenu.getOptions().isEmpty()) {
                event.reply("You do not have any items you can use.").setEphemeral(true).queue();
                return;
            }
            event.replyComponents(ActionRow.of(selectMenu.build())).setEphemeral(true).queue();
        } else if (split[2].equals("gift")) {
            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("inv-" + consumerId + "-gift");
            selectMenu.setPlaceholder("Select an item to gift");
            for (Economy.ShopItem item : Economy.getItems()) {
                if (consumer.getItemQuantity(item.getId()) > 0 && item.isGiftingEnabled()) {
                    Messager.debug("adding " + item.getId() + " to gift list");
                    selectMenu.addOption(item.getName(), String.valueOf(item.getId()));
                }
            }
            Messager.debug("selectmenu size: " + selectMenu.getOptions().size());
            if (selectMenu.getOptions().isEmpty()) {
                event.reply("You do not have any items you can gift.").setEphemeral(true).queue();
                return;
            }
            event.replyComponents(ActionRow.of(selectMenu.build())).setEphemeral(true).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String id = event.getSelectMenu().getId();
        if (id == null) return;
        SelectOption selected = event.getSelectedOptions().get(0);

        String[] split = id.split("-");

        if (!id.startsWith("inv-")) return;

        long consumerId = Long.parseLong(split[1]);

        if (consumerId != event.getUser().getIdLong()) {
            event.reply("Only the invoker of this command can interact with this.").setEphemeral(true).queue();
            return;
        }

        Economy.Consumer consumer = Economy.getConsumer(consumerId);
        switch (split[2]) {
            case "use" -> {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);

                int itemId = Integer.parseInt(selected.getValue());
                Economy.ShopItem item = Economy.getItem(itemId);
                if (item == null) {
                    event.replyEmbeds(embed.setDescription("That item could not be found.").build()).setEphemeral(true).queue();
                    return;
                }
                int itemQuantity = consumer.getItemQuantity(itemId);
                if (itemQuantity < 1) {
                    event.replyEmbeds(embed.setDescription("You do not have any of that item to use!").build()).setEphemeral(true).queue();
                    return;
                }
                if (item.isUseDisabled()) {
                    event.replyEmbeds(embed.setDescription("That item cannot be used.").build()).setEphemeral(true).queue();
                    return;
                }

                try {
                    item.use(Objects.requireNonNull(event.getMember()));
                } catch (UnsupportedOperationException e) {
                    event.replyEmbeds(embed.setDescription(e.getMessage()).build()).setEphemeral(true).queue();
                    return;
                }

                consumer.setItemQuantity(itemId, itemQuantity - 1);
                Economy.saveConsumer(consumer);

                embed.setColor(Color.GREEN).setDescription("Used one " + item.getName() + "!")
                        .setFooter("You now have " + consumer.getQuantityOfItem(itemId) + " of this item left.");
                event.replyEmbeds(embed.build()).queue();

                EmbedBuilder invEmbed = new EmbedBuilder();
                invEmbed.setTitle("Your Inventory");

                Map<Integer, Integer> items = consumer.getItems();

                if (items.isEmpty()) {
                    invEmbed.setDescription("You do not currently have any items. You can purchase items from the shop.");
                } else {
                    invEmbed = getInvEmbed(consumer);
                    if (invEmbed.getFields().isEmpty()) {
                        invEmbed.setDescription("You do not currently have any items. You can purchase items from the shop.");
                    }
                }

                event.getMessage().editMessageEmbeds(invEmbed.build()).queue();

            }
            case "gift" -> {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);

                int itemId = Integer.parseInt(selected.getValue());
                Economy.ShopItem item = Economy.getItem(itemId);
                if (item == null) {
                    event.replyEmbeds(embed.setDescription("That item could not be found.").build()).setEphemeral(true).queue();
                    return;
                }
                int itemQuantity = consumer.getItemQuantity(itemId);
                if (itemQuantity < 1) {
                    event.replyEmbeds(embed.setDescription("You do not have any of that item to gift!").build()).queue();
                    return;
                }
                if (!item.isGiftingEnabled()) {
                    event.replyEmbeds(embed.setDescription("That item cannot be gifted.").build()).setEphemeral(true).queue();
                    return;
                }

                EntitySelectMenu.Builder selectMenu = EntitySelectMenu.create("inv-" + consumerId + "-giftItem-" + item.getId(), EntitySelectMenu.SelectTarget.USER);
                selectMenu.setPlaceholder("Select a user to gift to").setRequiredRange(1, 1);

                event.editComponents(event.getMessage().getComponents().get(0), ActionRow.of(selectMenu.build())).queue();
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        String id = event.getSelectMenu().getId();
        if (id == null) return;
        Messager.debug("EntitySelectMenu with ID " + id);
        Mentions mentions = event.getMentions();

        String[] split = id.split("-");

        if (!id.startsWith("inv-")) return;

        long consumerId = Long.parseLong(split[1]);

        if (consumerId != event.getUser().getIdLong()) {
            event.reply("Only the invoker of this command can interact with this.").setEphemeral(true).queue();
            return;
        }

        Economy.Consumer consumer = Economy.getConsumer(consumerId);
        if (split[2].equals("giftItem")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.RED);

            int itemId = Integer.parseInt(split[3]);
            Economy.ShopItem item = Economy.getItem(itemId);
            if (item == null) {
                event.replyEmbeds(embed.setDescription("That item could not be found.").build()).setEphemeral(true).queue();
                return;
            }
            int itemQuantity = consumer.getItemQuantity(itemId);
            if (itemQuantity < 1) {
                event.replyEmbeds(embed.setDescription("You do not have any of that item to gift!").build()).setEphemeral(true).queue();
                return;
            }
            if (!item.isGiftingEnabled()) {
                event.replyEmbeds(embed.setDescription("That item cannot be gifted.").build()).setEphemeral(true).queue();
                return;
            }

            List<Member> members = mentions.getMembers();
            if (members.isEmpty()) {
                event.reply("You did not select anyone.").setEphemeral(true).queue();
                return;
            }

            Member member = members.get(0);

            Economy.Consumer consumer1 = Economy.getConsumer(member.getIdLong());
            consumer1.setItemQuantity(itemId, consumer1.getQuantityOfItem(itemId) + 1);
            consumer.setItemQuantity(itemId, consumer.getQuantityOfItem(itemId) - 1);

            Economy.saveConsumer(consumer);
            Economy.saveConsumer(consumer1);

            embed.setDescription(event.getUser().getAsMention() + " gifted one " + item.getName() + " to " + member.getAsMention() + ".")
                    .setColor(Color.GREEN).setFooter("You now have " + consumer.getQuantityOfItem(itemId) + " of this item left.");
            event.reply(member.getAsMention() + ", you were gifted an item!").addEmbeds(embed.build()).queue();

            EmbedBuilder invEmbed = new EmbedBuilder();
            invEmbed.setTitle("Your Inventory");

            Map<Integer, Integer> items = consumer.getItems();

            if (items.isEmpty()) {
                invEmbed.setDescription("You do not currently have any items. You can purchase items from the shop.");
            } else {
                invEmbed = getInvEmbed(consumer);
                if (invEmbed.getFields().isEmpty()) {
                    invEmbed.setDescription("You do not currently have any items. You can purchase items from the shop.");
                }
            }
        }
    }
}
