package rpg.api;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Optional;

/**
 * Cross-plugin surface over the monster/boss modules, primarily so orelia-world's
 * QuestModule can turn an {@code EntityDeathEvent} into a KILL_MONSTER/KILL_BOSS
 * objective update without depending on {@code rpg.monster}/{@code rpg.boss} internals.
 */
public interface CombatApi {

    /** The monsters.yml id this entity was spawned from, if it is a tagged Orelia monster. */
    Optional<String> identifyMonster(LivingEntity entity);

    /** The bosses.yml id wrapping this monster id, if that monster is also a boss. */
    Optional<String> identifyBoss(String monsterId);

    /** Spawns a plain monsters.yml-defined monster (no boss phases/abilities) at the given location. */
    Optional<LivingEntity> spawnMonster(String monsterId, Location location);

    /** Spawns a bosses.yml-defined boss at the given location (resolves its underlying monster, registers phases/abilities). */
    Optional<LivingEntity> spawnBoss(String bossId, Location location);
}
