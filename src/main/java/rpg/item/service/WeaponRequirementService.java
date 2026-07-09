package rpg.item.service;

import rpg.item.model.WeaponData;
import rpg.job.service.JobService;
import rpg.status.model.PlayerStatusComponent;
import rpg.status.service.StatusService;

import java.util.UUID;

/**
 * Checks whether a player is allowed to actually use (not just hold) a weapon: required
 * job and required level from {@link WeaponData}. Combines the job and status modules'
 * public services rather than duplicating their state.
 */
public final class WeaponRequirementService {

    private final JobService jobService;
    private final StatusService statusService;

    public WeaponRequirementService(JobService jobService, StatusService statusService) {
        this.jobService = jobService;
        this.statusService = statusService;
    }

    public boolean meetsRequirements(UUID uuid, WeaponData data) {
        if (!jobService.canUseWeaponType(uuid, data.getWeaponType())) {
            return false;
        }
        if (data.getRequiredJob() != null && jobService.getCurrentJob(uuid).filter(data.getRequiredJob()::equals).isEmpty()) {
            return false;
        }
        int level = statusService.component(uuid).map(PlayerStatusComponent::getLevel).orElse(1);
        return level >= data.getRequiredLevel();
    }
}
