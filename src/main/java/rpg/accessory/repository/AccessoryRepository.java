package rpg.accessory.repository;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.accessory.model.AccessoryData;
import rpg.accessory.model.AccessoryType;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry of every {@link AccessoryData}, rebuilt from {@code accessories.yml}.
 */
public final class AccessoryRepository {

    private Map<String, AccessoryData> accessories = new LinkedHashMap<>();

    public void load(YamlConfiguration config) {
        Map<String, AccessoryData> loaded = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("accessories");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection accessorySection = section.getConfigurationSection(id);
                if (accessorySection == null) {
                    continue;
                }
                loaded.put(id, parse(id, accessorySection));
            }
        }
        this.accessories = loaded;
    }

    private AccessoryData parse(String id, ConfigurationSection section) {
        StatSheet bonus = StatSheet.empty();
        ConfigurationSection bonusSection = section.getConfigurationSection("stat-bonus");
        if (bonusSection != null) {
            for (StatType type : StatType.values()) {
                bonus.set(type, bonusSection.getDouble(type.name(), 0));
            }
        }
        return new AccessoryData(
                id,
                section.getString("name", id),
                AccessoryType.valueOf(section.getString("type", "CHARM").trim().toUpperCase()),
                bonus,
                section.getStringList("description"),
                section.getInt("custom-model-data", 0));
    }

    public Optional<AccessoryData> findById(String id) {
        return Optional.ofNullable(accessories.get(id));
    }

    public Map<String, AccessoryData> getAll() {
        return Map.copyOf(accessories);
    }
}
