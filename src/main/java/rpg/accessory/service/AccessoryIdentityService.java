package rpg.accessory.service;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import rpg.accessory.model.AccessoryData;
import rpg.accessory.repository.AccessoryRepository;

import java.util.Optional;

/**
 * Resolves an {@link ItemStack} back to the {@link AccessoryData} it was generated from.
 */
public final class AccessoryIdentityService {

    private final AccessoryKeys keys;
    private final AccessoryRepository repository;

    public AccessoryIdentityService(AccessoryKeys keys, AccessoryRepository repository) {
        this.keys = keys;
        this.repository = repository;
    }

    public Optional<String> idOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        ItemMeta meta = stack.getItemMeta();
        return Optional.ofNullable(meta.getPersistentDataContainer().get(keys.accessoryId(), PersistentDataType.STRING));
    }

    public Optional<AccessoryData> dataOf(ItemStack stack) {
        return idOf(stack).flatMap(repository::findById);
    }
}
