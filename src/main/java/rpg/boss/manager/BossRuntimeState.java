package rpg.boss.manager;

/**
 * Per-spawned-boss runtime tracking: how many phases have already fired, and whether
 * enrage has kicked in. Not persisted - a boss encounter resets if the server restarts.
 */
public final class BossRuntimeState {

    private int phasesTriggered = 0;
    private boolean enraged = false;

    public int getPhasesTriggered() {
        return phasesTriggered;
    }

    public void incrementPhasesTriggered() {
        phasesTriggered++;
    }

    public boolean isEnraged() {
        return enraged;
    }

    public void setEnraged(boolean enraged) {
        this.enraged = enraged;
    }
}
