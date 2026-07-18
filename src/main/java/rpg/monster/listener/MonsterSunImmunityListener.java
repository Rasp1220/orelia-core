package rpg.monster.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import rpg.monster.service.MonsterSpawnService;

/**
 * Cancels sunlight-triggered combustion on tagged Orelia monsters (e.g. zombies burning at
 * dawn) while leaving other ignition sources alone. Sunlight burning fires the plain
 * {@link EntityCombustEvent} base class with no source (no block, no entity); combustion
 * caused by lava/fire blocks fires {@code EntityCombustByBlockEvent} and combustion caused by
 * another entity (e.g. a fire-elemental skill) fires {@code EntityCombustByEntityEvent} - both
 * are subclasses, so checking for the *exact* base class here only ever cancels the sunlight
 * case.
 */
public final class MonsterSunImmunityListener implements Listener {

    private final MonsterSpawnService spawnService;

    public MonsterSunImmunityListener(MonsterSpawnService spawnService) {
        this.spawnService = spawnService;
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (event.getClass() != EntityCombustEvent.class) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity entity) || spawnService.dataOf(entity).isEmpty()) {
            return;
        }
        event.setCancelled(true);
    }
}
