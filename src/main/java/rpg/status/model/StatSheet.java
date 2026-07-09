package rpg.status.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * A flat set of stat values. Used both as a single contribution (base stats, one piece
 * of equipment, one buff) and as the aggregated final result - {@link #plus} folds
 * contributions together.
 */
public final class StatSheet {

    private final Map<StatType, Double> values = new EnumMap<>(StatType.class);

    public static StatSheet empty() {
        return new StatSheet();
    }

    public StatSheet set(StatType type, double value) {
        values.put(type, value);
        return this;
    }

    public double get(StatType type) {
        return values.getOrDefault(type, 0.0);
    }

    public StatSheet plus(StatSheet other) {
        StatSheet result = new StatSheet();
        result.values.putAll(this.values);
        for (Map.Entry<StatType, Double> entry : other.values.entrySet()) {
            result.values.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
        return result;
    }

    public StatSheet copy() {
        StatSheet copy = new StatSheet();
        copy.values.putAll(this.values);
        return copy;
    }

    public Map<StatType, Double> asMap() {
        return Map.copyOf(values);
    }
}
