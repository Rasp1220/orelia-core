package rpg.accessory.model;

import rpg.status.model.StatSheet;

import java.util.List;

/**
 * Static accessory definition loaded from {@code accessories.yml}.
 */
public final class AccessoryData {

    private final String id;
    private final String name;
    private final AccessoryType type;
    private final StatSheet statBonus;
    private final List<String> description;
    private final int customModelData;

    public AccessoryData(String id, String name, AccessoryType type, StatSheet statBonus,
                          List<String> description, int customModelData) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.statBonus = statBonus;
        this.description = description;
        this.customModelData = customModelData;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AccessoryType getType() {
        return type;
    }

    public StatSheet getStatBonus() {
        return statBonus;
    }

    public List<String> getDescription() {
        return description;
    }

    public int getCustomModelData() {
        return customModelData;
    }
}
