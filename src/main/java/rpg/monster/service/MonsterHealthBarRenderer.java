package rpg.monster.service;

import rpg.util.MathUtil;

/**
 * Formats a monster's nametag as "{name} [health bar] {current}/{max}" (config-driven
 * template/colors/length), used both at spawn (full HP) and after every hit
 * ({@code MonsterHealthBarListener}). Pure/testable - takes already-resolved config values
 * rather than reading {@code config.yml} itself.
 */
public final class MonsterHealthBarRenderer {

    private static final String BAR_CHAR = "█"; // █

    public String render(String name, double currentHp, double maxHp, int length, String format,
                          String filledColor, String emptyColor) {
        double ratio = maxHp > 0 ? MathUtil.clamp(currentHp / maxHp, 0, 1) : 0;
        int filled = MathUtil.clamp((int) Math.round(ratio * length), 0, length);
        int empty = length - filled;

        String bar = filledColor + BAR_CHAR.repeat(filled) + emptyColor + BAR_CHAR.repeat(empty);

        return format
                .replace("{name}", name)
                .replace("{bar}", bar)
                .replace("{current}", String.valueOf((int) Math.ceil(Math.max(0, currentHp))))
                .replace("{max}", String.valueOf((int) Math.ceil(maxHp)));
    }
}
