package rpg.job.model;

import rpg.core.player.PlayerDataComponent;

import java.util.UUID;

/**
 * Which job the player currently has selected. {@code null} means unemployed (no weapon
 * restriction bonus applied, but also no passive bonus).
 */
public final class PlayerJobComponent implements PlayerDataComponent {

    private final UUID owner;
    private JobType currentJob;

    public PlayerJobComponent(UUID owner, JobType currentJob) {
        this.owner = owner;
        this.currentJob = currentJob;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public JobType getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(JobType currentJob) {
        this.currentJob = currentJob;
    }
}
