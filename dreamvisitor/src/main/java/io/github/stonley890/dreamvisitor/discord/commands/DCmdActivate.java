package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class DCmdActivate implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("activate", "Activate your account to gain access to the rest of the server.")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();

		if (guild == null) {
			event.reply("This only works in a guild.").setEphemeral(true).queue();
			return;
		}
		if (member == null) {
			event.reply("You are not in the guild.").setEphemeral(true).queue();
			return;
		}

		long roleId = Dreamvisitor.getPlugin().getConfig().getLong("memberRoleId");
		Dreamvisitor.debug("Role ID: " + roleId);
	    Role memberRole = guild.getRoleById(roleId);

		if (memberRole == null) {
			event.reply("The member role does not exist. Please contact a staff member.").setEphemeral(true).queue();
			return;
		}

		guild.addRoleToMember(member, memberRole).queue();
		event.reply("You are now activated!").setEphemeral(true).queue();
    }
}
