package rpg.boss.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import rpg.boss.manager.BossStateManager;
import rpg.boss.model.BossData;
import rpg.boss.repository.BossRepository;
import rpg.monster.service.MonsterSpawnService;

/**
 * Multiplies a boss's outgoing damage once it has entered enrage. Runs at {@code LOW},
 * after the monster module's {@code LOW}-priority attack-power handler (this module is
 * registered later, so it sees that already-applied base damage and scales it).
 */
public final class BossEnrageListener implements Listener {

    private final MonsterSpawnService monsterSpawnService;
    private final BossRepository bossRepository;
    private final BossStateManager stateManager;

    public BossEnrageListener(MonsterSpawnService monsterSpawnService, BossRepository bossRepository, BossStateManager stateManager) {
        this.monsterSpawnService = monsterSpawnService;
        this.bossRepository = bossRepository;
        this.stateManager = stateManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        BossData boss = monsterSpawnService.idOf(attacker).flatMap(bossRepository::findByMonsterId).orElse(null);
        if (boss == null) {
            return;
        }
        if (stateManager.stateOf(attacker.getUniqueId()).isEnraged()) {
            event.setDamage(event.getDamage() * boss.getEnrageDamageMultiplier());
        }
    }
}
