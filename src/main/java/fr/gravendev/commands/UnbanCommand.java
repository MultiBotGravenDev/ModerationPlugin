package fr.gravendev.commands;

import fr.gravendev.ModerationPlugin;
import fr.gravendev.sanctions.SanctionMember;
import fr.gravendev.sanctions.SanctionType;
import fr.gravendev.sanctions.SanctionsData;
import fr.gravendev.utils.EmbedTemplate;
import fr.neutronstars.nbot.api.command.CommandExecutor;
import fr.neutronstars.nbot.api.command.CommandType;
import fr.neutronstars.nbot.api.entity.NBotUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.Helpers;

import java.awt.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnbanCommand implements CommandExecutor {

    private static final Pattern mentionUserPattern = Pattern.compile("<@!?([0-9]{8,})>");
    private final ModerationPlugin moderationPlugin;
    private final String command;

    public UnbanCommand(ModerationPlugin moderationPlugin) {
        this.command = "unban";
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

        if (args.length == 0) {
            message.getChannel().sendMessage(EmbedTemplate.badArguments(command, "@membre")).queue();
            return false;
        }
        String id = extractId(args[0]);
        Guild guild = message.getGuild();

        if (id.length() > 20 || !Helpers.isNumeric(id)) {
            message.getChannel().sendMessage(EmbedTemplate.badArguments(command, "@membre")).queue();
            return false;
        }

        SanctionsData sanctionsData = moderationPlugin.getSanctionsData();
        SanctionMember sanctionMember = sanctionsData.getLast(id, SanctionType.BAN);
        if (sanctionMember != null) {
            sanctionMember.setEnd(new Date());
            sanctionMember.setFinished(true);
            sanctionsData.save(sanctionMember);

            User user = guild.getJDA().retrieveUserById(id).complete();
            TextChannel logs = moderationPlugin.getLogs();
            logs.sendMessage(EmbedTemplate.getRemovedSanction(sanctionMember.getType(), user, message.getAuthor())).queue();
        }

        guild.retrieveBanById(id).queue(
                ban -> unban(message, id),
                error -> message.getChannel().sendMessage(EmbedTemplate.buildEmbed(Color.RED, "Cet utilisateur n'est pas bannis")).queue());
        return false;
    }

    private void unban(Message message, String id) {
        Guild guild = message.getGuild();
        guild.unban(id).queue(success -> {
                    User user = guild.getJDA().getUserCache().getElementById(id);
                    message.getChannel().sendMessage(EmbedTemplate.buildEmbed(Color.DARK_GRAY, (user != null ? user.getName() : id) + " vient d'être unban")).queue();
                },
                error -> message.getChannel().sendMessage(EmbedTemplate.buildEmbed(Color.RED, "Impossible de débannir cet utilisateur")).queue());
    }

    private String extractId(String id) {
        Matcher matcher = mentionUserPattern.matcher(id);
        return matcher.find() ? matcher.group(1) : id;
    }

}
