package rpg.monster.service;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * PersistentDataContainer key used to tag spawned entities with their {@code monsters.yml} id.
 */
public final class MonsterKeys {

    private final NamespacedKey monsterId;

    public MonsterKeys(Plugin plugin) {
        this.monsterId = new NamespacedKey(plugin, "monster_id");
    }

    public NamespacedKey monsterId() {
        return monsterId;
    }
}
