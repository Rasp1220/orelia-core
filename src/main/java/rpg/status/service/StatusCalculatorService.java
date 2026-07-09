package rpg.status.service;

import rpg.status.model.ModifierType;
import rpg.status.model.PlayerStatusComponent;
import rpg.status.model.StatModifier;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;

/**
 * Pure computation: base stats (already level-scaled by {@link LevelGrowthService}) plus
 * every equipment contribution, then buffs applied flat-first then percent, per stat.
 * Holds no player state of its own so it is trivial to unit test.
 */
public final class StatusCalculatorService {

    public StatSheet calculateFinal(PlayerStatusComponent component) {
        StatSheet aggregate = component.getBaseStats().copy();
        for (StatSheet contribution : component.getEquipmentContributions()) {
            aggregate = aggregate.plus(contribution);
        }

        StatSheet result = StatSheet.empty();
        for (StatType type : StatType.values()) {
            double flatTotal = aggregate.get(type);
            double percentTotal = 0.0;
            for (StatModifier modifier : component.getBuffs()) {
                if (modifier.getStatType() != type) {
                    continue;
                }
                if (modifier.getModifierType() == ModifierType.FLAT) {
                    flatTotal += modifier.getAmount();
                } else {
                    percentTotal += modifier.getAmount();
                }
            }
            double finalValue = flatTotal * (1 + percentTotal / 100.0);
            result.set(type, Math.max(0, finalValue));
        }
        return result;
    }
}
