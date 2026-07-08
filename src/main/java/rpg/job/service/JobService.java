package rpg.job.service;

import rpg.core.player.PlayerDataManager;
import rpg.item.model.WeaponType;
import rpg.job.manager.JobManager;
import rpg.job.model.Job;
import rpg.job.model.JobType;
import rpg.job.model.PlayerJobComponent;
import rpg.status.service.StatusService;

import java.util.Optional;
import java.util.UUID;

/**
 * Public entry point for job changes and weapon-restriction checks. NPC and item/skill
 * modules call this instead of touching {@link JobManager} or the player data component
 * directly.
 */
public final class JobService {

    private static final String STATUS_SOURCE_KEY = "job";

    private final PlayerDataManager playerDataManager;
    private final JobManager jobManager;
    private final StatusService statusService;

    public JobService(PlayerDataManager playerDataManager, JobManager jobManager, StatusService statusService) {
        this.playerDataManager = playerDataManager;
        this.jobManager = jobManager;
        this.statusService = statusService;
    }

    public Optional<JobType> getCurrentJob(UUID uuid) {
        return component(uuid).map(PlayerJobComponent::getCurrentJob);
    }

    /**
     * Changes the player's job and immediately swaps their passive stat bonus in the
     * status module. Returns false if the requested job has no config definition.
     */
    public boolean changeJob(UUID uuid, JobType newJob) {
        Optional<Job> definition = jobManager.getDefinition(newJob);
        if (definition.isEmpty()) {
            return false;
        }
        component(uuid).ifPresent(component -> component.setCurrentJob(newJob));
        statusService.setEquipmentContribution(uuid, STATUS_SOURCE_KEY, definition.get().getPassiveBonus());
        return true;
    }

    public boolean canUseWeaponType(UUID uuid, WeaponType weaponType) {
        JobType currentJob = getCurrentJob(uuid).orElse(null);
        if (currentJob == null) {
            return false;
        }
        return jobManager.getDefinition(currentJob).map(job -> job.canUse(weaponType)).orElse(false);
    }

    private Optional<PlayerJobComponent> component(UUID uuid) {
        return playerDataManager.get(uuid).flatMap(data -> data.component(PlayerJobComponent.class));
    }
}
