package rpg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates {@code &}-coded strings (as used across every messages.yml / items.yml entry)
 * into legacy section-coded text or Adventure {@link Component}s. {@code &#RRGGBB} hex codes
 * are also supported: {@link #colorize} expands them into the legacy {@code §x§R§R§G§G§B§B}
 * encoding Bukkit/Adventure use for hex under the hood, so both {@link #colorize} and
 * {@link #component} handle hex transparently - callers never need to special-case it.
 */
public final class ColorUtil {

    private static final char AMPERSAND = '&';
    private static final char SECTION = '§';
    private static final String COLOR_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    private static final LegacyComponentSerializer HEX_AWARE_SERIALIZER = LegacyComponentSerializer.builder()
            .character(SECTION)
            .hexColors()
            .build();

    private ColorUtil() {
    }

    public static String colorize(String input) {
        if (input == null) {
            return "";
        }
        char[] chars = expandHex(input).toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == AMPERSAND && COLOR_CODES.indexOf(chars[i + 1]) > -1) {
                chars[i] = SECTION;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public static Component component(String input) {
        return HEX_AWARE_SERIALIZER.deserialize(colorize(input));
    }

    /** Expands {@code &#RRGGBB} into {@code §x§R§R§G§G§B§B} (each hex digit as its own {@code §}-prefixed char). */
    private static String expandHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        matcher.reset();
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            StringBuilder expanded = new StringBuilder().append(SECTION).append('x');
            for (char hexDigit : matcher.group(1).toLowerCase().toCharArray()) {
                expanded.append(SECTION).append(hexDigit);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(expanded.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
