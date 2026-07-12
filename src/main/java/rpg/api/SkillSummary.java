package rpg.api;

/** One learned skill entry, returned by {@link SkillApi#getLearnedSkills}. */
public record SkillSummary(String id, String name, int level, int maxLevel) {
}
