package rpg.item.model;

import org.bukkit.ChatColor;

/**
 * Weapon rarity tiers. Order matters (ordinal is used for GUI sorting), colors are used
 * to prefix generated item names.
 */
public enum Rarity {
    COMMON(ChatColor.WHITE),
    UNCOMMON(ChatColor.GREEN),
    RARE(ChatColor.AQUA),
    EPIC(ChatColor.LIGHT_PURPLE),
    LEGENDARY(ChatColor.GOLD);

    private final ChatColor color;

    Rarity(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }
}
