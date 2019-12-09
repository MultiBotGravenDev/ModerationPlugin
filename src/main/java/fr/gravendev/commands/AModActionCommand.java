package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionMember;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.utils.EmbedTemplate;
import fr.gravendev.utils.TimeUtils;
import fr.neutronstars.nbot.api.command.CommandExecutor;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AModActionCommand implements CommandExecutor {

    private final ModerationPlugin plugin;
    private String command;

    AModActionCommand(String command, ModerationPlugin moderationPlugin) {
        this.command = command;
        this.plugin = moderationPlugin;
    }

    public String name() {
        return command;
    }

    protected abstract boolean isTemporary();

    protected abstract SanctionType getType();

    @Override
    public boolean onCommand(NBotUser user, Message message, String... args) {
        if (message.getMember() == null) {
            return false;
        }

        MessageChannel messageChannel = message.getChannel();

        List<Member> mentionedMembers = message.getMentionedMembers();
        if (mentionedMembers.size() == 0 || args.length < (isTemporary() ? 2 : 1)) {
            messageChannel.sendMessage(EmbedTemplate.badArguments(command, "@membre " + (isTemporary() ? "<durée> " : "") + "<raison>")).queue();
            return false;
        }

        Guild guild = message.getGuild();
        Member memberVictim = mentionedMembers.get(0);
        Member bot = guild.getMember(message.getJDA().getSelfUser());

        if (!PermissionUtil.canInteract(bot, memberVictim)) {
            messageChannel.sendMessage(EmbedTemplate.buildEmbed(Color.RED, "Impossible d'appliquer une sanction sur cet utilisateur !")).queue();
            return false;
        }

        String reason = getReason(args);
        Date start = new Date();
        Date end = isTemporary() ? getEnd(args[1]) : null;

        if (isTemporary() && end == null) {
            messageChannel.sendMessage(EmbedTemplate.buildEmbed(Color.RED, "Durée invalide")).queue();
            return false;
        }

        TextChannel logs = getPlugin().getLogs();
        logs.sendMessage(EmbedTemplate.getLogsSanction(getType(), memberVictim.getUser(), user.getUser(), end, reason)).queue();

        messageChannel.sendMessage(EmbedTemplate.getMessageSanction(getType(), memberVictim.getUser(), user.getUser(), end, reason)).queue();

        if (punish(message, user, memberVictim, start, end, reason)) {
            SanctionMember sanctionMember = new SanctionMember(memberVictim.getId(), user.getUser().getId(), reason, getType(), start, end);
            getPlugin().getSanctionsData().save(sanctionMember);
        }

        return false;
    }

    private String getReason(String[] args) {
        return args.length >= (isTemporary() ? 3 : 2)
                ? Stream.of(args)
                .skip(isTemporary() ? 2 : 1)
                .collect(Collectors.joining(" "))
                : "Non Définie";
    }

    private Date getEnd(String period) {
        if (!isTemporary()) {
            return null;
        }

        long duration = TimeUtils.parsePeriod(period);
        Date end = new Date(System.currentTimeMillis() + duration);
        return duration == -1 ? null : end;
    }

    protected abstract boolean punish(Message message, NBotUser moderator, Member victim, Date start, Date end, String reason);

    ModerationPlugin getPlugin() {
        return plugin;
    }
}
