package rpg.api;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Cross-plugin surface over the item (weapon) module.
 */
public interface ItemApi {

    Optional<ItemStack> createWeapon(String weaponId);

    /** Resolves an ItemStack back to the weapon id it was generated from, if any. */
    Optional<String> identifyWeapon(ItemStack stack);

    Set<String> getAllWeaponIds();

    /** Whether {@code playerId} currently meets the job/level requirement to use this weapon id. */
    boolean weaponMeetsRequirements(UUID playerId, String weaponId);

    int getEnhancementLevel(ItemStack stack);

    /** Increments the weapon's enhancement level by one and returns the new level. */
    int enhanceWeapon(ItemStack stack);
}
