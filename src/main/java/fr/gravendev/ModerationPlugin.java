package fr.gravendev;

import fr.gravendev.commands.*;
import fr.gravendev.events.MemberListeners;
import fr.gravendev.sanctions.SanctionsData;
import fr.gravendev.tasks.SanctionsTask;
import fr.neutronstars.nbot.api.NBot;
import fr.neutronstars.nbot.api.command.CommandType;
import fr.neutronstars.nbot.api.event.Listener;
import fr.neutronstars.nbot.api.event.server.NBotServerStartedEvent;
import fr.neutronstars.nbot.api.event.server.NBotServerStartingEvent;
import fr.neutronstars.nbot.api.plugin.NBotPlugin;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.Arrays;
import java.util.Timer;

public class ModerationPlugin extends NBotPlugin {

    private SanctionsData sanctionsData;

    private Guild guild;
    private Role muted;
    private TextChannel logs;

    public ModerationPlugin() {
        super("moderation_plugin", "Moderation Plugin", "1.0", "", "Nolan");
    }

    @Listener
    public void onStarting(NBotServerStartingEvent event) {
        super.getLogger().info("Starting of " + super.getName() + "...");

        DatabasePlugin databasePlugin = (DatabasePlugin) NBot.get().getPluginManager().getPlugin(DatabasePlugin.class).get();
        this.sanctionsData = new SanctionsData(databasePlugin.getDatabaseManager());

        registerCommand(new KickCommand(this));
        registerCommand(new WarnCommand(this));
        registerCommand(new MuteCommand("mute", false, this));
        registerCommand(new MuteCommand("tempmute", true, this));
        registerCommand(new BanCommand("ban", false, this));
        registerCommand(new BanCommand("tempban", true, this));
        new InfractionsCommand(this);
        new UnbanCommand(this);
        new UnmuteCommand(this);
    }

    @Listener
    public void onStarted(NBotServerStartedEvent event) {
        ShardManager shardManager = NBot.get().getShardManager();

        initGuild(shardManager);
        initMuted();
        initLogs();

        Timer timer = new Timer();
        timer.schedule(new SanctionsTask(this.guild, this), 0, 10_000L);
        shardManager.addEventListener(new MemberListeners(this));
    }

    public SanctionsData getSanctionsData() {
        return sanctionsData;
    }

    private void registerCommand(AModActionCommand command) {
        super.registerCommand(command.name(),
                "command." + command.name() + ".description",
                command,
                super.getId() + ".command." + command.name(),
                CommandType.ADMINISTRATOR);
    }

    private void initGuild(ShardManager shardManager) {
        String guildIdentifier = getConfiguration("guild");
        this.guild = shardManager.getGuildById(guildIdentifier);
        if (this.guild == null) {
            getLogger().error("Guild " + guildIdentifier + " not found !");
        }
    }

    private void initMuted() {
        String mutedIdentifier = getConfiguration("roles", "muted");
        this.muted = this.guild.getRoleById(mutedIdentifier);
        if (this.muted == null) {
            getLogger().error("Role muted not found ! (" + mutedIdentifier + ")");
        }
    }

    private void initLogs() {
        String logsIdentifier = getConfiguration("channels", "logs");
        this.logs = this.guild.getTextChannelById(logsIdentifier);
        if (this.logs == null) {
            getLogger().error("Channel logs not found ! (" + logsIdentifier + ")");
        }
    }

    private String getConfiguration(String... nodes) {
        String snowflake = super.getConfiguration().get("", nodes);
        if (snowflake.length() > 20 || !Helpers.isNumeric(snowflake)) {
            getLogger().error(Arrays.toString(nodes) + " is not a valid snowflake.");
        }
        return snowflake;
    }

    public Role getMuted() {
        return muted;
    }

    public TextChannel getLogs() {
        return logs;
    }
}
