package rpg.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Cross-plugin surface for debug/testplay tooling (orelia-debug) to inspect and edit any
 * registered config file at runtime without hand-editing YAML + {@code /oladmin reload}.
 * Each plugin publishes its own {@code DebugApi} over its own {@code rpg.core.config.
 * ConfigManager}, so orelia-debug can target core/world/extra config files uniformly.
 */
public interface DebugApi {

    /** Names of every config file registered with this plugin's ConfigManager (e.g. "config.yml"). */
    Set<String> listConfigFiles();

    /** Current value at {@code path} in {@code fileName}, stringified. Empty if missing/unregistered. */
    Optional<String> getConfigValue(String fileName, String path);

    /**
     * Sets {@code path} in {@code fileName} to {@code rawValue} (parsed as boolean/long/double,
     * falling back to a plain string) and saves immediately. Returns {@code false} if
     * {@code fileName} isn't registered.
     */
    boolean setConfigValue(String fileName, String path, String rawValue);

    /** Force-saves {@code fileName} to disk. No-op if {@code fileName} isn't registered. */
    void saveConfig(String fileName);

    /** Every key path in {@code fileName}, deep, sorted - for a "confighelp" listing. */
    List<String> describeConfigKeys(String fileName);

    /**
     * Whether {@code playerId}'s debug mode is currently enabled - while on, job/level weapon-use
     * and skill-cast requirement checks are bypassed for that player. {@code false} if the player
     * isn't online (the flag lives only on their in-memory {@code PlayerData}).
     */
    boolean isDebugMode(UUID playerId);

    /** Enables/disables debug mode for {@code playerId}. No-op (returns {@code false}) if they aren't online. */
    boolean setDebugMode(UUID playerId, boolean enabled);
}
