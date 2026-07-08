package rpg.api;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;

/**
 * Cross-plugin surface over the accessory module.
 */
public interface AccessoryApi {

    Optional<ItemStack> createAccessory(String accessoryId);

    Set<String> getAllAccessoryIds();
}
