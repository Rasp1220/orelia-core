package rpg.api;

import rpg.skill.service.SkillProgressService;

import java.util.UUID;

final class SkillApiImpl implements SkillApi {

    private final SkillProgressService progressService;

    SkillApiImpl(SkillProgressService progressService) {
        this.progressService = progressService;
    }

    @Override
    public void grantSkillPoints(UUID playerId, int amount) {
        progressService.grantSkillPoints(playerId, amount);
    }

    @Override
    public int getSkillLevel(UUID playerId, String skillId) {
        return progressService.getSkillLevel(playerId, skillId);
    }
}
