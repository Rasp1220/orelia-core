package rpg.api;

import java.util.UUID;

/**
 * Cross-plugin surface over the skill module - primarily so orelia-world's QuestModule
 * can grant skill points as a quest reward.
 */
public interface SkillApi {

    void grantSkillPoints(UUID playerId, int amount);

    int getSkillLevel(UUID playerId, String skillId);
}
