package rpg.status.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import rpg.status.combat.DamageFormula;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;
import rpg.status.service.StatusService;

/**
 * Folds ATK (attacker) and DEF (victim) into vanilla melee/projectile damage via
 * {@link DamageFormula}. Weapon/skill damage (including their own crit rolls) is applied
 * separately before this listener runs, since skills fire a fresh damage event of their own.
 */
public final class CombatStatusListener implements Listener {

    private final StatusService statusService;

    public CombatStatusListener(StatusService statusService) {
        this.statusService = statusService;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        double damage = event.getDamage();

        if (event.getDamager() instanceof Player attacker) {
            StatSheet stats = statusService.getFinalStats(attacker.getUniqueId()).orElse(null);
            if (stats != null) {
                damage = DamageFormula.applyAttackBonus(damage, stats.get(StatType.ATK));
            }
        }

        if (event.getEntity() instanceof Player victim) {
            StatSheet stats = statusService.getFinalStats(victim.getUniqueId()).orElse(null);
            if (stats != null) {
                damage = DamageFormula.mitigate(damage, stats.get(StatType.DEF));
            }
        }

        event.setDamage(Math.max(0, damage));
    }
}
