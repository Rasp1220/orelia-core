package rpg.api;

import java.util.List;
import java.util.UUID;

/**
 * Cross-plugin surface over the skill module - primarily so orelia-world's QuestModule
 * can grant skill points as a quest reward.
 */
public interface SkillApi {

    void grantSkillPoints(UUID playerId, int amount);

    int getSkillLevel(UUID playerId, String skillId);

    /** Every skill the player has learned (level &gt; 0), across all weapon types. */
    List<SkillSummary> getLearnedSkills(UUID playerId);
}
