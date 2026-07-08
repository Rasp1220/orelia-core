package rpg.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates {@code &}-coded strings (as used across every messages.yml / items.yml entry)
 * into {@link ChatColor}-formatted text.
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    public static String colorize(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> colorize(List<String> input) {
        return input.stream().map(ColorUtil::colorize).collect(Collectors.toList());
    }
}
