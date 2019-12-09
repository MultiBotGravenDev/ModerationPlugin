package fr.gravendev.sanctions;

public enum SanctionType {

    BAN("banni"),
    MUTE("rendu muet"),
    KICK("éjecté"),
    WARN("averti");

    private String inSentence;

    SanctionType(String inSentence) {
        this.inSentence = inSentence;
    }

    public String getInSentence() {
        return inSentence;
    }
}
