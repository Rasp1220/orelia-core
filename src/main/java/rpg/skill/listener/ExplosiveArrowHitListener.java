package rpg.skill.listener;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ProjectileSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import rpg.skill.executor.ExplosiveArrowExecutor;

/**
 * Detonates arrows tagged by {@link ExplosiveArrowExecutor} (爆裂矢) on impact: entity
 * damage only in a radius around the hit point, no block damage, so the skill can't be
 * used to grief terrain.
 */
public final class ExplosiveArrowHitListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }
        if (!arrow.hasMetadata(ExplosiveArrowExecutor.EXPLOSIVE_METADATA)) {
            return;
        }
        double[] payload = (double[]) arrow.getMetadata(ExplosiveArrowExecutor.EXPLOSIVE_METADATA).get(0).value();
        double amount = payload[0];
        double radius = payload[1];

        Location impact = arrow.getLocation();
        ProjectileSource shooter = arrow.getShooter();
        Player caster = shooter instanceof Player player ? player : null;

        impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 1);
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        for (LivingEntity target : impact.getWorld().getNearbyLivingEntities(impact, radius, radius, radius)) {
            if (target.equals(caster)) {
                continue;
            }
            if (caster != null) {
                target.damage(amount, caster);
            } else {
                target.damage(amount);
            }
        }
        arrow.remove();
    }
}
