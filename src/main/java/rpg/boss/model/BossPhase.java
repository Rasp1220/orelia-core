package rpg.boss.model;

/**
 * One boss phase transition: triggers the first time the boss's HP drops to or below
 * {@code hpThresholdPercent}, broadcasting {@code announceMessage} (SOW section 14 "演出").
 */
public final class BossPhase {

    private final double hpThresholdPercent;
    private final String announceMessage;

    public BossPhase(double hpThresholdPercent, String announceMessage) {
        this.hpThresholdPercent = hpThresholdPercent;
        this.announceMessage = announceMessage;
    }

    public double getHpThresholdPercent() {
        return hpThresholdPercent;
    }

    public String getAnnounceMessage() {
        return announceMessage;
    }
}
