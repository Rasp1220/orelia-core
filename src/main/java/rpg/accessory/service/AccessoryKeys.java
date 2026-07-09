package rpg.accessory.service;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * PersistentDataContainer key used to stamp accessory identity onto generated ItemStacks.
 */
public final class AccessoryKeys {

    private final NamespacedKey accessoryId;

    public AccessoryKeys(Plugin plugin) {
        this.accessoryId = new NamespacedKey(plugin, "accessory_id");
    }

    public NamespacedKey accessoryId() {
        return accessoryId;
    }
}
