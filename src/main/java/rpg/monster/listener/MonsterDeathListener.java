package rpg.monster.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import rpg.monster.model.MonsterData;
import rpg.monster.service.MonsterDropService;
import rpg.monster.service.MonsterSpawnService;

/**
 * Rolls the drop table and grants EXP/money to the killer when a tagged monster dies.
 */
public final class MonsterDeathListener implements Listener {

    private final MonsterSpawnService spawnService;
    private final MonsterDropService dropService;

    public MonsterDeathListener(MonsterSpawnService spawnService, MonsterDropService dropService) {
        this.spawnService = spawnService;
        this.dropService = dropService;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        MonsterData data = spawnService.dataOf(event.getEntity()).orElse(null);
        if (data == null) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        dropService.rewardKiller(data, killer, event.getEntity().getLocation());
    }
}
