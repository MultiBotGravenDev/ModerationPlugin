package fr.gravendev.sanctions;


import java.util.Date;
import java.util.UUID;

public class SanctionMember {

    private final UUID uuid;
    private final String user;
    private final String moderator;
    private final String reason;
    private final SanctionType type;
    private final Date start;
    private Date end;
    private boolean finished;

    SanctionMember(UUID uuid, String user, String moderator, String reason, SanctionType type, Date start, Date end, boolean finished) {
        this.uuid = uuid;
        this.user = user;
        this.moderator = moderator;
        this.reason = reason;
        this.type = type;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public SanctionMember(String user, String moderator, String reason, SanctionType type, Date start, Date end) {
        this.uuid = UUID.randomUUID();
        this.user = user;
        this.moderator = moderator;
        this.reason = reason;
        this.type = type;
        this.start = start;
        this.end = end;
        this.finished = false;
    }

    UUID getUUID() {
        return uuid;
    }

    public String getUser() {
        return user;
    }

    String getModerator() {
        return moderator;
    }

    public String getReason() {
        return reason;
    }

    public SanctionType getType() {
        return type;
    }

    public Date getStart() {
        return start;
    }

    Date getEnd() {
        return end;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
