package rpg.skill.executor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import rpg.item.listener.WeaponUseListener;
import rpg.item.model.WeaponData;
import rpg.item.service.WeaponIdentityService;
import rpg.skill.model.SkillData;

/**
 * Shared helpers every {@link SkillExecutor} archetype uses: computing a skill's base
 * damage off the caster's equipped weapon, and delivering it through the normal Bukkit
 * damage event while telling {@link WeaponUseListener} not to overwrite it.
 */
public final class SkillDamage {

    private final Plugin plugin;
    private final WeaponIdentityService identityService;

    public SkillDamage(Plugin plugin, WeaponIdentityService identityService) {
        this.plugin = plugin;
        this.identityService = identityService;
    }

    public double baseDamage(Player caster, SkillData data, int skillLevel) {
        var weapon = caster.getInventory().getItemInMainHand();
        double attackPower = identityService.dataOf(weapon)
                .map(WeaponData::getAttackPower)
                .orElse(1.0);
        return attackPower * identityService.enhancementMultiplier(weapon) * data.scaledDamageMultiplier(skillLevel);
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
