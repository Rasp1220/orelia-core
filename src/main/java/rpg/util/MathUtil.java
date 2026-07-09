package rpg.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Small numeric helpers shared by status/skill/economy calculations, avoiding a handful
 * of duplicated clamp/roll implementations across modules.
 */
public final class MathUtil {

    private MathUtil() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns true with the given probability, where {@code chancePercent} is in the
     * range [0, 100]. Used for crit rolls, skill procs and drop chances.
     */
    public static boolean rollChance(double chancePercent) {
        return ThreadLocalRandom.current().nextDouble(0, 100) < chancePercent;
    }

    public static double lerp(double start, double end, double t) {
        return start + (end - start) * clamp(t, 0, 1);
    }
}
