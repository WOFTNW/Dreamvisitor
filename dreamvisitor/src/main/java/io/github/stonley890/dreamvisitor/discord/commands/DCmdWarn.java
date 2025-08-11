package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.AltFamily;
import io.github.stonley890.dreamvisitor.data.Infraction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.InvalidObjectException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DCmdWarn extends ListenerAdapter implements DiscordCommand {

    public static InteractionHook lastInteraction = null;
    public static Infraction lastInfraction = null;
    public static boolean silent = false;
    public static long memberId = 0;

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("warn", "Warn a member.")
                .addOption(OptionType.USER, "member", "The member to warn", true)
                .addOption(OptionType.INTEGER, "value", "How many infractions to count this as.", true)
                .addOption(OptionType.BOOLEAN, "silent", "Whether to notify the member. If true, Dreamvisitor will NOT notify.", true)
                .addOption(OptionType.STRING, "reason", "The reason for this warn.", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue(DCmdWarn::updateLastInteraction);

        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        if (member == null) {
            event.getHook().editOriginal("That member does not exist.").queue();
            return;
        }

        long parent = AltFamily.getParent(member.getIdLong());
        if (parent != member.getIdLong()) {
            Objects.requireNonNull(event.getGuild()).retrieveMemberById(parent).queue(parentMember -> event.getHook().editOriginal("That user is the child of " + parentMember.getAsMention() + ". Warn them instead.").queue());
            return;
        }

        int value = Objects.requireNonNull(event.getOption("value")).getAsInt();
        if (value > Infraction.BAN_POINT) {
            event.getHook().editOriginal("Slow down there. You cannot give an infraction a value higher than **" + Infraction.BAN_POINT + "**.").queue();
            return;
        }

        List<Infraction> infractions;

        infractions = Infraction.getInfractions(member.getIdLong());

        int infractionCount = Infraction.getInfractionCount(infractions, false);
        if (infractionCount + value > Infraction.BAN_POINT) {
            event.getHook().editOriginal("You cannot add an infraction whose value exceeds the ban point of " + Infraction.BAN_POINT +
                    ". The highest value you can assign is " + (Infraction.BAN_POINT - infractionCount)).queue();
            return;
        }

        String reason = event.getOption("reason", OptionMapping::getAsString);
        if (reason == null) reason = "Unspecified.";
        reason = reason.strip();

        boolean silent = Boolean.TRUE.equals(event.getOption("silent", OptionMapping::getAsBoolean));

        boolean hasTempban;

        hasTempban = Infraction.hasTempban(member.getIdLong());

        if (infractionCount < Infraction.BAN_POINT) {
            if (infractionCount + value == Infraction.BAN_POINT) {

                ActionRow actionRow;
                if (!hasTempban) {
                    if (silent) actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Ban them for two weeks."),
                            Button.secondary(Infraction.actionNoBan, "Don't auto-ban them.")
                    );
                    else actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Ban them for two weeks."),
                            Button.primary(Infraction.actionUserBan, "No, but mention a suspension in the message."),
                            Button.secondary(Infraction.actionNoBan, "No, don't mention a suspension.")
                    );
                } else {
                    if (silent) actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Permanently ban from Minecraft."),
                            Button.danger(Infraction.actionAllBan, "Permanently ban from all."),
                            Button.secondary(Infraction.actionNoBan, "No, don't ban them.")
                    );
                    else actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Permanently ban from Minecraft."),
                            Button.danger(Infraction.actionAllBan, "Ban from all immediately (skip message)."),
                            Button.primary(Infraction.actionUserBan, "Mention a ban, but don't auto-ban."),
                            Button.secondary(Infraction.actionNoBan, "Don't mention a ban and don't auto-ban.")
                    );
                }

                memberId = member.getIdLong();

                lastInfraction = new Infraction((byte) value, reason, LocalDateTime.now());
                DCmdWarn.silent = silent;
                try {
                    event.getHook().editOriginal("This will be the user's third warn. Do you want me to also give them a ban from the Minecraft server?")
                            .setActionRow(actionRow.getComponents()).queue();
                } catch (Exception e) {
                    Bukkit.getLogger().warning("There was a problem responding to a /warn command: " + e.getMessage());
                }
            } else {

                try {
                    Infraction.execute(new Infraction((byte) value, reason, LocalDateTime.now()), member, silent, Infraction.actionNoBan);
                } catch (Exception e) {
                    try {
                        event.getHook().editOriginal("There was a problem executing this command: " + e.getMessage()).queue();
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("There was a problem responding to a /warn command: " + ex.getMessage());
                    }
                    return;
                }

                try {
                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setDescription("Recorded infraction of value " + value + " for " + member.getAsMention() + ".")
                                    .addField("Reason", reason, false);

                    event.getHook().editOriginalEmbeds(embed.build()).queue(null, throwable -> Bukkit.getLogger().warning("There was a problem responding to a /warn command: " + throwable.getMessage()));
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("There was a problem responding to a /warn command: " + e.getMessage());
                }

            }
        }
    }

    private static void updateLastInteraction(InteractionHook newInteraction) {

        List<ActionRow> actionRows = new ArrayList<>();

        if (lastInteraction != null) {
            lastInteraction.retrieveOriginal().queue(original -> {
                for (ActionRow actionRow : original.getActionRows()) {
                    actionRows.add(actionRow.asDisabled());
                }
            }, null);
            if (!actionRows.isEmpty()) lastInteraction.editOriginalComponents(actionRows).queue(completion -> lastInteraction = newInteraction);
        }

        lastInteraction = newInteraction;

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        switch (buttonId) {
            case Infraction.actionBan, Infraction.actionNoBan, Infraction.actionAllBan, Infraction.actionUserBan -> {

                try {
                    event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
                    Infraction.execute(DCmdWarn.lastInfraction, Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).retrieveMemberById(DCmdWarn.memberId).complete()), DCmdWarn.silent, buttonId);
                    event.reply("Infraction notice created.").queue();
                } catch (InsufficientPermissionException e) {
                    event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
                    event.reply("Dreamvisitor does not have sufficient permissions! " + e.getMessage()).queue();
                } catch (InvalidObjectException e) {
                    event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
                    event.reply("Something is configured incorrectly! " + e.getMessage()).queue();
                }
            }
            case "warn-understand" -> {
                TextChannel channel = (TextChannel) event.getMessageChannel();
                event.reply("Marked as dismissed. View permission removed.").queue();
                try {
                    channel.upsertPermissionOverride(Objects.requireNonNull(event.getMember())).setDenied(Permission.VIEW_CHANNEL).queue();
                } catch (InsufficientPermissionException e) {
                    event.reply("Dreamvisitor has insufficient permissions: " + e.getMessage()).queue();
                    return;
                }
                event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
            }
            case "warn-explain" -> {
                event.reply("A staff member will explain the reason for your warn. Please be patient. This could take some time.").queue();
                event.getMessage().editMessageComponents(event.getMessage().getActionRows().get(0).asDisabled()).queue();
            }
        }
    }
}
