package rpg.skill.service;

import rpg.core.player.PlayerDataManager;
import rpg.skill.model.PlayerSkillComponent;
import rpg.skill.model.SkillData;
import rpg.skill.repository.SkillRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Learn/upgrade skills by spending skill points earned from quests (SOW section 11).
 */
public final class SkillProgressService {

    private final PlayerDataManager playerDataManager;
    private final SkillRepository skillRepository;

    public SkillProgressService(PlayerDataManager playerDataManager, SkillRepository skillRepository) {
        this.playerDataManager = playerDataManager;
        this.skillRepository = skillRepository;
    }

    public int getSkillLevel(UUID uuid, String skillId) {
        return component(uuid).map(c -> c.getSkillLevel(skillId)).orElse(0);
    }

    public void grantSkillPoints(UUID uuid, int amount) {
        component(uuid).ifPresent(c -> c.addSkillPoints(amount));
    }

    /**
     * Spends one skill point to raise {@code skillId} by one level, capped at the skill's
     * {@code max-level}. Returns false if the player lacks points, is already at max
     * level, or the skill id is unknown.
     */
    public boolean upgradeSkill(UUID uuid, String skillId) {
        Optional<SkillData> data = skillRepository.findById(skillId);
        Optional<PlayerSkillComponent> component = component(uuid);
        if (data.isEmpty() || component.isEmpty()) {
            return false;
        }
        PlayerSkillComponent c = component.get();
        int current = c.getSkillLevel(skillId);
        if (current >= data.get().getMaxLevel()) {
            return false;
        }
        if (!c.spendSkillPoint()) {
            return false;
        }
        c.setSkillLevel(skillId, current + 1);
        return true;
    }

    private Optional<PlayerSkillComponent> component(UUID uuid) {
        return playerDataManager.get(uuid).flatMap(d -> d.component(PlayerSkillComponent.class));
    }
}
