package fr.gravendev.events;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionMember;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.sanctions.SanctionsData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class MemberListeners extends ListenerAdapter {

    private final SanctionsData sanctionsData;
    private final Role muted;

    public MemberListeners(ModerationPlugin moderationPlugin) {
        this.sanctionsData = moderationPlugin.getSanctionsData();
        this.muted = moderationPlugin.getMuted();
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        SanctionMember lastMute = sanctionsData.getLast(member.getId(), SanctionType.MUTE);
        if (!lastMute.isFinished()) {
            guild.addRoleToMember(member, muted).queue();
        }
    }
}
