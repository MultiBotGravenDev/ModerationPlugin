package fr.gravendev.tasks;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionMember;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.sanctions.SanctionsData;
import fr.gravendev.utils.EmbedTemplate;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.TimerTask;

public class SanctionsTask extends TimerTask {

    private final Guild guild;
    private final TextChannel logs;
    private final SanctionsData sanctionsData;
    private final Role muted;

    public SanctionsTask(Guild guild, ModerationPlugin moderationPlugin) {
        this.guild = guild;
        this.logs = moderationPlugin.getLogs();
        this.sanctionsData = moderationPlugin.getSanctionsData();
        this.muted = moderationPlugin.getMuted();
    }

    @Override
    public void run() {
        List<SanctionMember> allFinished = this.sanctionsData.getAllFinished();
        for (SanctionMember sanctionMember : allFinished) {
            action(sanctionMember);
            sanctionMember.setFinished(true);
            sanctionsData.save(sanctionMember);
        }
    }

    private void action(SanctionMember sanctionMember) {
        if (sanctionMember.getType() == SanctionType.MUTE) {
            this.muteAction(sanctionMember);
            return;
        }
        this.banAction(sanctionMember);
    }

    private void banAction(SanctionMember sanctionMember) {
        guild.unban(sanctionMember.getUser()).queue();
        User user = guild.getJDA().retrieveUserById(sanctionMember.getUser()).complete();
        logs.sendMessage(EmbedTemplate.getRemovedSanction(sanctionMember.getType(), user, null)).queue();
    }

    private void muteAction(SanctionMember sanctionMember) {
        Member member = guild.getMemberById(sanctionMember.getUser());
        if (member == null) {
            return;
        }

        logs.sendMessage(EmbedTemplate.getRemovedSanction(sanctionMember.getType(), member.getUser(), null)).queue();

        guild.removeRoleFromMember(member, muted).queue();
        member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Vous avez été unmute du discord GravenDev").queue());
    }
}
