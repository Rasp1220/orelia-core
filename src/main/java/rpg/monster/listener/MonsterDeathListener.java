package rpg.monster.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import rpg.core.message.MessageManager;
import rpg.monster.model.MonsterData;
import rpg.monster.service.MonsterDropService;
import rpg.monster.service.MonsterSpawnService;
import rpg.monster.spawnpoint.service.MonsterSpawnPointService;
import rpg.util.ColorUtil;

/**
 * Rolls the drop table and grants EXP/money to the killer when a tagged monster dies, and
 * frees up its spawn point's alive-count slot (if it came from one) regardless of killer.
 * Also rewrites the death message when a player is killed by a tagged monster, since the
 * entity's live nametag ({@link rpg.monster.service.MonsterHealthBarRenderer}'s HP bar) would
 * otherwise leak into vanilla's default "X was slain by <killer's display name>" message.
 */
public final class MonsterDeathListener implements Listener {

    private final MonsterSpawnService spawnService;
    private final MonsterDropService dropService;
    private final MonsterSpawnPointService spawnPointService;
    private final MessageManager messages;

    public MonsterDeathListener(MonsterSpawnService spawnService, MonsterDropService dropService,
                                 MonsterSpawnPointService spawnPointService, MessageManager messages) {
        this.spawnService = spawnService;
        this.dropService = dropService;
        this.spawnPointService = spawnPointService;
        this.messages = messages;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        MonsterData data = spawnService.dataOf(event.getEntity()).orElse(null);
        if (data == null) {
            return;
        }
        spawnPointService.onEntityRemoved(event.getEntity());

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        dropService.rewardKiller(data, killer, event.getEntity().getLocation());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent byEntity)) {
            return;
        }
        if (!(byEntity.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        MonsterData data = spawnService.dataOf(attacker).orElse(null);
        if (data == null) {
            return;
        }
        String formatted = messages.format("monster.death-message",
                "player", event.getEntity().getName(), "monster", data.getName());
        event.deathMessage(ColorUtil.component(formatted));
    }
}
