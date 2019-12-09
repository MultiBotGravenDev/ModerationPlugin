package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.utils.EmbedTemplate;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.Date;

public class MuteCommand extends AModActionCommand {

    private boolean temporary;

    public MuteCommand(String command, boolean temporary, ModerationPlugin moderationPlugin) {
        super(command, moderationPlugin);
        this.temporary = temporary;
    }

    @Override
    protected boolean isTemporary() {
        return temporary;
    }

    @Override
    protected SanctionType getType() {
        return SanctionType.MUTE;
    }

    @Override
    protected boolean punish(Message message, NBotUser moderator, Member victim, Date start, Date end, String reason) {
        Guild guild = message.getGuild();

        long hasMuted = victim.getRoles().stream().filter(role -> role.getName().equals("Muted")).count();
        if (hasMuted != 0) {
            message.getChannel().sendMessage(EmbedTemplate.buildEmbed(Color.RED, "Ce membre est déjà mute")).queue();
            return false;
        }

        Role muted = getPlugin().getMuted();
        guild.addRoleToMember(victim, muted).queue();
        return true;
    }
}
