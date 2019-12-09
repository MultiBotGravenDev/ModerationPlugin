package fr.gravendev.utils;

import fr.gravendev.sanctions.SanctionType;
import fr.neutronstars.nbot.api.NBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.Date;

public class EmbedTemplate {

    public static MessageEmbed badArguments(String command, String text) {
        return buildEmbed(Color.RED, "Utilisation: " + NBot.get().getCommandManager().getPrefix() + command + " " + text);
    }

    public static MessageEmbed buildEmbed(Color color, String message) {
        return new EmbedBuilder()
                .setColor(color)
                .setTitle(message)
                .build();
    }

    public static MessageEmbed getMessageSanction(SanctionType type, User victim, User moderator, Date end, String reason) {
        return new EmbedBuilder()
                .setThumbnail(victim.getEffectiveAvatarUrl())
                .setColor(new Color(245, 80, 69))
                .setTitle(victim.getName() + " a été " + type.getInSentence())
                .setDescription("**Par:** " + moderator.getName() +
                        "\n**Raison:** " + reason +
                        (end != null ? "\n**Fin le:** " + TimeUtils.getDateFormat().format(end) : ""))
                .build();
    }

    public static MessageEmbed getLogsSanction(SanctionType type, User victim, User moderator, Date end, String reason) {
        return new EmbedBuilder()
                .setThumbnail(victim.getEffectiveAvatarUrl())
                .setColor(new Color(245, 80, 69))
                .setTitle("[" + type.name() + "] " + victim.getAsTag())
                .setDescription("**Par:** " + moderator.getName() +
                        "\n**Raison:** " + reason +
                        (end != null ? "\n**Fin le:** " + TimeUtils.getDateFormat().format(end) : ""))
                .setFooter(victim.getId())
                .build();
    }

    public static MessageEmbed getRemovedSanction(SanctionType type, User victim, User moderator) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setThumbnail(victim.getEffectiveAvatarUrl())
                .setColor(new Color(63, 242, 78))
                .setTitle("[Un" + type.name().toLowerCase() + "] " + victim.getAsTag());

        if (moderator != null) {
            embedBuilder.setDescription("**Par:** " + moderator.getAsTag());
        }

        return embedBuilder.build();
    }
}
