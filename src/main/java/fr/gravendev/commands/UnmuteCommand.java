package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionMember;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.sanctions.SanctionsData;
import fr.gravendev.utils.EmbedTemplate;
import fr.neutronstars.nbot.api.command.CommandExecutor;
import fr.neutronstars.nbot.api.command.CommandType;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.Date;
import java.util.List;

public class UnmuteCommand implements CommandExecutor {

    private final ModerationPlugin moderationPlugin;
    private final String command;

    public UnmuteCommand(ModerationPlugin moderationPlugin) {
        this.command = "unmute";
        this.moderationPlugin = moderationPlugin;
        moderationPlugin.registerCommand(command,
                "command." + command + ".description",
                this,
                moderationPlugin.getId() + ".command." + command,
                CommandType.ADMINISTRATOR);
    }

    @Override
    public boolean onCommand(NBotUser nBotUser, Message message, String... args) {
        if (message.getMember() == null) {
            return false;
        }

        Guild guild = message.getGuild();
        MessageChannel messageChannel = message.getChannel();
        List<Member> mentionedMembers = message.getMentionedMembers();

        if (mentionedMembers.size() == 0) {
            messageChannel.sendMessage(EmbedTemplate.badArguments(command, "@membre")).queue();
            return false;
        }

        Member victim = mentionedMembers.get(0);
        long hasMuted = victim.getRoles().stream().filter(role -> role.getName().equals("Muted")).count();
        if (hasMuted == 0) {
            message.getChannel().sendMessage(EmbedTemplate.buildEmbed(Color.RED, "Ce membre n'est pas mute !")).queue();
            return false;
        }

        Role muted = moderationPlugin.getMuted();
        guild.removeRoleFromMember(victim, muted).queue();

        SanctionsData sanctionsData = moderationPlugin.getSanctionsData();
        SanctionMember sanctionMember = sanctionsData.getLast(victim.getId(), SanctionType.MUTE);
        if (sanctionMember != null) {
            sanctionMember.setEnd(new Date());
            sanctionMember.setFinished(true);
            sanctionsData.save(sanctionMember);

            TextChannel logs = moderationPlugin.getLogs();
            logs.sendMessage(EmbedTemplate.getRemovedSanction(sanctionMember.getType(), victim.getUser(), message.getAuthor())).queue();
        }

        message.getChannel().sendMessage(EmbedTemplate.buildEmbed(Color.DARK_GRAY, victim.getUser().getAsTag() + " vient d'être unmute")).queue();
        victim.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Vous avez été unmute").queue());
        return false;
    }
}