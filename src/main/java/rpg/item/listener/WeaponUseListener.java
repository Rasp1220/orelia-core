package rpg.item.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import rpg.core.message.MessageManager;
import rpg.item.model.WeaponData;
import rpg.item.service.WeaponIdentityService;
import rpg.item.service.WeaponRequirementService;
import rpg.status.combat.DamageFormula;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;
import rpg.status.service.StatusService;

/**
 * Turns a melee hit with an Orelia weapon into base damage from {@link WeaponData}
 * (attack power, crit roll folding in the attacker's CRT/CRT_DMG stats via
 * {@link DamageFormula}). Runs at {@link EventPriority#LOW} so the status module's
 * ATK/DEF combat listener (default priority) applies its percentage modifiers on top of
 * this base value instead of overwriting it.
 */
public final class WeaponUseListener implements Listener {

    /**
     * Metadata key the skill module sets on the caster while a skill's own damage event
     * is in flight, so this listener does not overwrite the skill's damage with the
     * weapon's plain attack power. See {@code rpg.skill.executor}.
     */
    public static final String SKILL_OVERRIDE_METADATA = "orelia_skill_active";

    private final Plugin plugin;
    private final WeaponIdentityService identityService;
    private final WeaponRequirementService requirementService;
    private final StatusService statusService;
    private final MessageManager messages;

    public WeaponUseListener(Plugin plugin, WeaponIdentityService identityService, WeaponRequirementService requirementService,
                              StatusService statusService, MessageManager messages) {
        this.plugin = plugin;
        this.identityService = identityService;
        this.requirementService = requirementService;
        this.statusService = statusService;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWeaponHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (attacker.hasMetadata(SKILL_OVERRIDE_METADATA)) {
            return;
        }
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        WeaponData data = identityService.dataOf(weapon).orElse(null);
        if (data == null) {
            return;
        }

        if (!requirementService.meetsRequirements(attacker.getUniqueId(), data)) {
            event.setCancelled(true);
            messages.send(attacker, "item.requirement-not-met");
            return;
        }

        StatSheet stats = statusService.getFinalStats(attacker.getUniqueId()).orElse(null);
        double critRate = data.getCritRate() + (stats != null ? stats.get(StatType.CRT) : 0);
        double damage = data.getAttackPower() * identityService.enhancementMultiplier(weapon);
        if (DamageFormula.rollCrit(critRate)) {
            double critDmg = stats != null ? stats.get(StatType.CRT_DMG) : 0;
            damage *= DamageFormula.criticalMultiplier(data.getCritMultiplier(), critDmg);
            attacker.setMetadata(DamageFormula.CRIT_METADATA_KEY, new FixedMetadataValue(plugin, true));
        } else {
            attacker.removeMetadata(DamageFormula.CRIT_METADATA_KEY, plugin);
        }
        event.setDamage(damage);
    }
}
