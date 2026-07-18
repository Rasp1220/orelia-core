package rpg.monster.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import rpg.monster.service.DamageDisplayService;
import rpg.status.combat.DamageFormula;

/**
 * Shows a floating damage number wherever a {@link LivingEntity} takes damage - works for
 * monsters and players alike. Runs at {@link EventPriority#MONITOR}, after damage is fully
 * resolved, and reads (then clears) {@link DamageFormula#CRIT_METADATA_KEY} off the damager
 * to color/scale crits differently from normal hits.
 */
public final class DamageDisplayListener implements Listener {

    private final Plugin plugin;
    private final DamageDisplayService displayService;

    public DamageDisplayListener(Plugin plugin, DamageDisplayService displayService) {
        this.plugin = plugin;
        this.displayService = displayService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        boolean isCrit = false;
        if (event instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof LivingEntity attacker) {
            isCrit = attacker.hasMetadata(DamageFormula.CRIT_METADATA_KEY);
            attacker.removeMetadata(DamageFormula.CRIT_METADATA_KEY, plugin);
        }
        displayService.show(victim.getEyeLocation(), event.getFinalDamage(), isCrit);
    }
}
