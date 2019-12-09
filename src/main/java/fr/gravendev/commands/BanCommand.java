package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionType;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Date;

public class BanCommand extends AModActionCommand {

    private final boolean temporary;

    public BanCommand(String command, boolean temporary, ModerationPlugin moderationPlugin) {
        super(command, moderationPlugin);
        this.temporary = temporary;
    }

    @Override
    protected boolean isTemporary() {
        return temporary;
    }

    @Override
    protected SanctionType getType() {
        return SanctionType.BAN;
    }

    @Override
    protected boolean punish(Message message, NBotUser moderator, Member victim, Date start, Date end, String reason) {
        message.getGuild().ban(victim, 0, reason).queue();
        return true;
    }
}
