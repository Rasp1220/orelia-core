package rpg.skill.listener;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import rpg.skill.executor.ArrowVolleyExecutor;

/**
 * Applies the bow skill damage multiplier {@link ArrowVolleyExecutor} stamps onto its
 * arrows, on top of vanilla bow damage (which already reflects draw strength).
 */
public final class ArrowSkillDamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }
        if (!arrow.hasMetadata(ArrowVolleyExecutor.DAMAGE_MULTIPLIER_METADATA)) {
            return;
        }
        double multiplier = arrow.getMetadata(ArrowVolleyExecutor.DAMAGE_MULTIPLIER_METADATA).get(0).asDouble();
        event.setDamage(event.getDamage() * multiplier);
    }
}
