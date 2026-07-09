package rpg.status.service;

import rpg.status.config.StatusGrowthConfig;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;

/**
 * Turns a character level into a base {@link StatSheet}, using the per-stat base value
 * and per-level growth configured in {@code config.yml: status.growth}.
 */
public final class LevelGrowthService {

    private final StatusGrowthConfig growthConfig;

    public LevelGrowthService(StatusGrowthConfig growthConfig) {
        this.growthConfig = growthConfig;
    }

    public StatSheet baseStatsForLevel(int level) {
        StatSheet sheet = StatSheet.empty();
        for (StatType type : StatType.values()) {
            double base = growthConfig.getBaseValue(type);
            double perLevel = growthConfig.getPerLevel(type);
            sheet.set(type, base + perLevel * Math.max(0, level - 1));
        }
        return sheet;
    }
}
