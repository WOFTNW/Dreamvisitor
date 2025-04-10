package io.github.stonley890.dreamvisitor.discord.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class DCmdBaltop implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("baltop", "Get the balances of the richest members.")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        final String currencySymbol = Economy.getCurrencySymbol();

        List<Economy.Consumer> sortedConsumers = Economy.getConsumers();
        sortedConsumers.sort(Comparator.comparingDouble(Economy.Consumer::getBalance).reversed());

        if (Dreamvisitor.debugMode) {
            Dreamvisitor.debug("Sorted consumers: ");
            for (Economy.Consumer sortedConsumer : sortedConsumers) {
                Dreamvisitor.debug(sortedConsumer.getId() + ": " + sortedConsumer.getBalance());
            }
        }

        final Economy.Consumer senderConsumer = Economy.getConsumer(event.getUser().getIdLong());

        final int senderIndex = sortedConsumers.indexOf(senderConsumer);
        Dreamvisitor.debug("Sender index: " + senderIndex);

        Economy.Consumer aboveSender = null;
        try {
            aboveSender = sortedConsumers.get(senderIndex - 1);
        } catch (IndexOutOfBoundsException ignored) {}
        Dreamvisitor.debug("Index above sender: " + (senderIndex - 1) + " id " + (aboveSender != null ? aboveSender.getId() : "null"));

        Economy.Consumer belowSender = null;
        try {
            belowSender = sortedConsumers.get(senderIndex + 1);
        } catch (IndexOutOfBoundsException ignored) {}
        Dreamvisitor.debug("Index below sender: " + (senderIndex + 1) + " id " + (belowSender != null ? belowSender.getId() : "null"));

        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Top Balances");

        int numberShown = 10;

        if (sortedConsumers.size() < numberShown) numberShown = sortedConsumers.size();
        Dreamvisitor.debug("Consumer list size: " + sortedConsumers.size());

        final List<Long> retrieveIds = new ArrayList<>(sortedConsumers.subList(0, numberShown).stream().map(Economy.Consumer::getId).toList());
        if (aboveSender != null) retrieveIds.add(aboveSender.getId());
        if (belowSender != null) retrieveIds.add(belowSender.getId());

        int finalNumberShown = numberShown;
        Economy.Consumer finalAboveSender = aboveSender;
        Economy.Consumer finalBelowSender = belowSender;
        Objects.requireNonNull(event.getGuild()).retrieveMembersByIds(retrieveIds).onSuccess(members -> {

            Map<Long, Member> memberMap = new HashMap<>();
            List<Member> topMembers = members.subList(0, finalNumberShown);
            for (Member member : topMembers) {
                memberMap.put(member.getIdLong(), member);
            }

            // Create a list to hold the sorted members
            List<Member> sortedMembers = new ArrayList<>();

            // Add members to the sorted list in the order of consumer IDs
            for (Economy.Consumer consumer : sortedConsumers) {
                Member member = memberMap.get(consumer.getId());
                sortedMembers.add(member);
            }

            final StringBuilder balanceList = new StringBuilder();
            for (int i = 0; i < finalNumberShown; i++) {
                balanceList.append(i + 1).append(". ")
                        .append(currencySymbol).append(Economy.formatDouble(sortedConsumers.get(i).getBalance())).append(": ");
                if (sortedMembers.get(i) == null) balanceList.append("<@").append(sortedConsumers.get(i).getId()).append(">\n");
                else balanceList.append(sortedMembers.get(i).getAsMention()).append("\n");
            }

            if (finalNumberShown == 0) balanceList.append("No one has any ").append(Economy.getCurrencySymbol()).append(" yet!\n");

            if (senderConsumer.getBalance() != 0) {
                balanceList.append("\nYour current rank is ").append(senderIndex + 1);
                if (finalAboveSender != null) {
                    Optional<Member> member = members.stream().filter(thisMember -> thisMember.getIdLong() == finalAboveSender.getId()).findFirst();
                    member.ifPresent(value -> balanceList.append(", behind ").append(value.getAsMention()).append(" (").append(currencySymbol).append(finalAboveSender.getBalance()).append(")"));
                }
                if (finalBelowSender != null) {
                    Optional<Member> member = members.stream().filter(thisMember -> thisMember.getIdLong() == finalBelowSender.getId()).findFirst();
                    member.ifPresent(value -> balanceList.append(", ahead of ").append(value.getAsMention()).append(" (").append(currencySymbol).append(finalBelowSender.getBalance()).append(")"));
                }
                balanceList.append(".");
            }

            embed.setDescription(balanceList).setFooter("Your current balance is " + Economy.formatDouble(senderConsumer.getBalance()) + ".");
            event.replyEmbeds(embed.build()).queue();
        });


    }
}
