package rpg.effect.repository;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.effect.model.EffectData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry of every {@link EffectData}, rebuilt from {@code effects.yml}.
 */
public final class EffectRepository {

    private Map<String, EffectData> effects = new LinkedHashMap<>();

    public void load(YamlConfiguration config) {
        Map<String, EffectData> loaded = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("effects");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection effectSection = section.getConfigurationSection(id);
                if (effectSection == null) {
                    continue;
                }
                loaded.put(id, parse(id, effectSection));
            }
        }
        this.effects = loaded;
    }

    private EffectData parse(String id, ConfigurationSection section) {
        return new EffectData(
                id,
                section.getString("particle", "CLOUD"),
                section.getInt("particle-count", 15),
                section.getDouble("spread-x", 0.5),
                section.getDouble("spread-y", 0.5),
                section.getDouble("spread-z", 0.5),
                section.getString("sound"),
                (float) section.getDouble("sound-volume", 1.0),
                (float) section.getDouble("sound-pitch", 1.0));
    }

    public Optional<EffectData> findById(String id) {
        return Optional.ofNullable(effects.get(id));
    }

    public Map<String, EffectData> getAll() {
        return Map.copyOf(effects);
    }
}
