package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionMember;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.sanctions.SanctionsData;
import fr.gravendev.utils.EmbedTemplate;
import fr.gravendev.utils.TimeUtils;
import fr.neutronstars.nbot.api.command.CommandExecutor;
import fr.neutronstars.nbot.api.command.CommandType;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class InfractionsCommand implements CommandExecutor {

    private final String command;
    private final SanctionsData sanctionsData;

    public InfractionsCommand(ModerationPlugin moderationPlugin) {
        this.command = "infractions";
        this.sanctionsData = moderationPlugin.getSanctionsData();
        moderationPlugin.registerCommand(command,
                "command." + command + ".description",
                this,
                moderationPlugin.getId() + ".command." + command,
                CommandType.ADMINISTRATOR);
    }

    @Override
    public boolean onCommand(NBotUser nBotUser, Message message, String... strings) {
        if (message.getMember() == null) {
            return false;
        }
        List<Member> mentionedMembers = message.getMentionedMembers();
        if (mentionedMembers.size() == 0) {
            message.getChannel().sendMessage(EmbedTemplate.badArguments(this.command, "@membre")).queue();
            return false;
        }

        Member member = mentionedMembers.get(0);

        List<SanctionMember> allInfractions = sanctionsData.getAll(member.getUser().getId(), SanctionType.WARN);

        Date lastDayDate = Date.from(Instant.now().minusSeconds(60 * 60 * 24));
        Date lastWeekDate = Date.from(Instant.now().minusSeconds(60 * 60 * 24 * 7));

        long lastWeek = allInfractions.stream().filter(infraction -> infraction.getStart().after(lastWeekDate)).count();
        long lastDay = allInfractions.stream().filter(infraction -> infraction.getStart().after(lastDayDate)).count();
        int total = allInfractions.size();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.RED)
                .setAuthor("Infractions de " + member.getUser().getAsTag(), member.getUser().getAvatarUrl())
                .addField("24 dernières heures", lastDay + " infraction" + (lastDay > 1 ? "s" : ""), true)
                .addField("7 derniers jours", lastWeek + " infraction" + (lastWeek > 1 ? "s" : ""), true)
                .addField("Total", total + " infraction" + (total > 1 ? "s" : ""), true);

        if (allInfractions.size() == 0) {
            embedBuilder.addField("10 dernières infractions", "Aucune", false);
        } else {

            StringBuilder builder = new StringBuilder();

            allInfractions.stream().limit(10).forEach(infraction -> builder.append("**")
                    .append(infraction.getReason())
                    .append("** - ")
                    .append(TimeUtils.getDateFormat().format(infraction.getStart()))
                    .append("\n"));
            embedBuilder.addField("10 dernières infractions", builder.toString(), false);
        }

        message.getChannel().sendMessage(embedBuilder.build()).queue();
        return false;
    }

}
