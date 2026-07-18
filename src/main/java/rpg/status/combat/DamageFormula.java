package rpg.status.combat;

import rpg.util.MathUtil;

/**
 * The single source of truth for damage math shared across the weapon/skill/monster/status
 * combat listeners (SOW combat rules) - kept pure (no Bukkit dependency) so it's unit
 * testable in isolation.
 */
public final class DamageFormula {

    /**
     * Bukkit metadata key set on the attacking entity whenever {@link #rollCrit} lands, so a
     * later listener (e.g. the floating damage-number display) can tell a crit apart from a
     * normal hit without re-rolling. Callers must clear this when a hit is NOT a crit, so a
     * stale flag never leaks into the next attack.
     */
    public static final String CRIT_METADATA_KEY = "orelia_last_hit_crit";

    private DamageFormula() {
    }

    /** {@code damage * (1 - defense/(defense+100))} - the shared defense-mitigation curve. */
    public static double mitigate(double damage, double defense) {
        return damage * (1 - defense / (defense + 100.0));
    }

    /** {@code damage * (1 + atkPercent/100)} - the shared attacker-stat bonus curve. */
    public static double applyAttackBonus(double damage, double atkPercent) {
        return damage * (1 + atkPercent / 100.0);
    }

    /**
     * The multiplier to apply on a crit: the weapon's/monster's own base crit multiplier
     * plus the attacker's {@code CRT_DMG} stat as an additive percentage bonus (e.g. a 1.5x
     * weapon with 20 CRT_DMG deals 1.5 + 20/100 = 1.7x).
     */
    public static double criticalMultiplier(double baseCritMultiplier, double critDmgPercent) {
        return baseCritMultiplier + critDmgPercent / 100.0;
    }

    /** Thin, semantically-named wrapper over {@link MathUtil#rollChance(double)} for crit rolls. */
    public static boolean rollCrit(double critRatePercent) {
        return MathUtil.rollChance(critRatePercent);
    }
}
