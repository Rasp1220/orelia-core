package rpg.item.service;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * PersistentDataContainer keys used to stamp identity onto generated weapon ItemStacks.
 * Centralized here so skill/accessory/gui modules read the same keys instead of each
 * hard-coding their own namespaced string.
 */
public final class WeaponKeys {

    private final NamespacedKey weaponId;
    private final NamespacedKey enhancementLevel;

    public WeaponKeys(Plugin plugin) {
        this.weaponId = new NamespacedKey(plugin, "weapon_id");
        this.enhancementLevel = new NamespacedKey(plugin, "enhancement_level");
    }

    public NamespacedKey weaponId() {
        return weaponId;
    }

    /** Level applied by the "強化屋" (enhancement shop) NPC - see {@link WeaponIdentityService#enhance}. */
    public NamespacedKey enhancementLevel() {
        return enhancementLevel;
    }
}
