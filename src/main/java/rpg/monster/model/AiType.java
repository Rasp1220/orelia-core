package rpg.monster.model;

/**
 * Coarse combat behavior hint read by {@link rpg.monster.service.MonsterSpawnService}
 * when configuring the vanilla mob's target selectors.
 */
public enum AiType {
    PASSIVE,
    AGGRESSIVE,
    RANGED
}
