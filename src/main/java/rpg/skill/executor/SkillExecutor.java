package rpg.skill.executor;

import org.bukkit.entity.Player;
import rpg.skill.model.SkillData;

/**
 * Runs one skill "archetype" (cone melee, self-centered AoE, dash strike, arrow volley, ...).
 * Multiple {@link SkillData} entries in skills.yml can point at the same executor via
 * {@code executor-type}, which is how new skills are added without new Java code as long
 * as their behavior matches an existing archetype.
 */
public interface SkillExecutor {

    void execute(Player caster, SkillData data, int skillLevel);
}
