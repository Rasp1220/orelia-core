package rpg.skill.executor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import rpg.item.listener.WeaponUseListener;
import rpg.item.model.WeaponData;
import rpg.item.service.WeaponIdentityService;
import rpg.skill.model.SkillData;
import rpg.status.combat.DamageFormula;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;
import rpg.status.service.StatusService;

/**
 * Shared helpers every {@link SkillExecutor} archetype uses: computing a skill's base
 * damage off the caster's equipped weapon (including a crit roll via {@link DamageFormula},
 * same as a plain weapon hit), and delivering it through the normal Bukkit damage event
 * while telling {@link WeaponUseListener} not to overwrite it.
 */
public final class SkillDamage {

    private final Plugin plugin;
    private final WeaponIdentityService identityService;
    private final StatusService statusService;

    public SkillDamage(Plugin plugin, WeaponIdentityService identityService, StatusService statusService) {
        this.plugin = plugin;
        this.identityService = identityService;
        this.statusService = statusService;
    }

    public double baseDamage(Player caster, SkillData data, int skillLevel) {
        var weapon = caster.getInventory().getItemInMainHand();
        WeaponData weaponData = identityService.dataOf(weapon).orElse(null);
        double attackPower = weaponData != null ? weaponData.getAttackPower() : 1.0;
        double damage = attackPower * identityService.enhancementMultiplier(weapon) * data.scaledDamageMultiplier(skillLevel);

        double critRate = weaponData != null ? weaponData.getCritRate() : 0.0;
        StatSheet stats = statusService.getFinalStats(caster.getUniqueId()).orElse(null);
        critRate += stats != null ? stats.get(StatType.CRT) : 0;
        if (DamageFormula.rollCrit(critRate)) {
            double baseCritMultiplier = weaponData != null ? weaponData.getCritMultiplier() : 1.5;
            double critDmg = stats != null ? stats.get(StatType.CRT_DMG) : 0;
            damage *= DamageFormula.criticalMultiplier(baseCritMultiplier, critDmg);
            caster.setMetadata(DamageFormula.CRIT_METADATA_KEY, new FixedMetadataValue(plugin, true));
        } else {
            caster.removeMetadata(DamageFormula.CRIT_METADATA_KEY, plugin);
        }
        return damage;
    }

    public void apply(Player caster, LivingEntity target, double amount) {
        caster.setMetadata(WeaponUseListener.SKILL_OVERRIDE_METADATA, new FixedMetadataValue(plugin, true));
        try {
            target.damage(amount, caster);
        } finally {
            caster.removeMetadata(WeaponUseListener.SKILL_OVERRIDE_METADATA, plugin);
        }
    }
}
