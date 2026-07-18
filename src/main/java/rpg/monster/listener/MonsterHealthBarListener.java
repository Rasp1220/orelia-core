package rpg.monster.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import rpg.monster.model.MonsterData;
import rpg.monster.service.MonsterSpawnService;

/**
 * Keeps a tagged monster's nametag HP bar in sync after every hit - listens to the generic
 * {@link EntityDamageEvent} (not just by-entity) so fall/fire/other environmental damage
 * updates the bar too. Runs at {@link EventPriority#MONITOR}: purely a read-only display
 * update, does not affect the already-finalized damage.
 */
public final class MonsterHealthBarListener implements Listener {

    private final MonsterSpawnService spawnService;

    public MonsterHealthBarListener(MonsterSpawnService spawnService) {
        this.spawnService = spawnService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        MonsterData data = spawnService.dataOf(entity).orElse(null);
        if (data == null) {
            return;
        }
        var maxHealthAttribute = entity.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttribute != null ? maxHealthAttribute.getValue() : data.getHp();
        double newHealth = Math.max(0, entity.getHealth() - event.getFinalDamage());
        spawnService.updateHealthBar(entity, data, newHealth, maxHealth);
    }
}
