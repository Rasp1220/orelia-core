package rpg.util;

/**
 * Formats durations for skill cooldowns / buff timers displayed in action bars and GUIs.
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    public static String formatSeconds(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }
}
