package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionType;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Date;

public class KickCommand extends AModActionCommand {

    public KickCommand(ModerationPlugin moderationPlugin) {
        super("kick", moderationPlugin);
    }

    @Override
    protected boolean isTemporary() {
        return false;
    }

    @Override
    protected SanctionType getType() {
        return SanctionType.KICK;
    }

    @Override
    protected boolean punish(Message message, NBotUser moderator, Member victim, Date start, Date end, String reason) {
        message.getGuild().kick(victim, reason).queue();
        return true;
    }
}
