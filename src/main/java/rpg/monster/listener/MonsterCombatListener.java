package rpg.monster.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import rpg.item.model.ElementType;
import rpg.item.model.WeaponData;
import rpg.item.service.WeaponIdentityService;
import rpg.monster.model.MonsterData;
import rpg.monster.service.MonsterSpawnService;
import rpg.status.combat.DamageFormula;

/**
 * Applies monster-side attack power (when the monster is the damager) and defense (when
 * the monster is the victim) on top of vanilla damage, mirroring how
 * {@link rpg.status.listener.CombatStatusListener} treats player ATK/DEF. Also applies a
 * flat weakness multiplier when the attacker's equipped weapon element matches the
 * monster's configured weak point, and rolls the monster's own crit chance via
 * {@link DamageFormula} when it is the attacker.
 */
public final class MonsterCombatListener implements Listener {

    private static final double WEAKNESS_MULTIPLIER = 1.5;

    private final Plugin plugin;
    private final MonsterSpawnService spawnService;
    private final WeaponIdentityService weaponIdentityService;

    public MonsterCombatListener(Plugin plugin, MonsterSpawnService spawnService, WeaponIdentityService weaponIdentityService) {
        this.plugin = plugin;
        this.spawnService = spawnService;
        this.weaponIdentityService = weaponIdentityService;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMonsterDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity attacker) {
            MonsterData data = spawnService.dataOf(attacker).orElse(null);
            if (data != null) {
                double damage = data.getAttackPower();
                if (DamageFormula.rollCrit(data.getCritRate())) {
                    damage *= DamageFormula.criticalMultiplier(data.getCritMultiplier(), 0);
                    attacker.setMetadata(DamageFormula.CRIT_METADATA_KEY, new FixedMetadataValue(plugin, true));
                } else {
                    attacker.removeMetadata(DamageFormula.CRIT_METADATA_KEY, plugin);
                }
                event.setDamage(damage);
            }
        }

        if (event.getEntity() instanceof LivingEntity victim && event.getDamager() instanceof Player attacker) {
            MonsterData data = spawnService.dataOf(victim).orElse(null);
            if (data != null) {
                double damage = DamageFormula.mitigate(event.getDamage(), data.getDefense());
                if (isWeaknessHit(attacker, data)) {
                    damage *= WEAKNESS_MULTIPLIER;
                }
                event.setDamage(Math.max(0, damage));
            }
        }
    }

    private boolean isWeaknessHit(Player attacker, MonsterData data) {
        if (data.getWeakness() == ElementType.NONE) {
            return false;
        }
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        return weaponIdentityService.dataOf(weapon)
                .map(WeaponData::getElement)
                .map(element -> element == data.getWeakness())
                .orElse(false);
    }
}
