package rpg.api;

import org.bukkit.inventory.ItemStack;
import rpg.item.manager.ItemManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class ItemApiImpl implements ItemApi {

    private final ItemManager itemManager;

    ItemApiImpl(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public Optional<ItemStack> createWeapon(String weaponId) {
        return itemManager.createWeapon(weaponId);
    }

    @Override
    public Optional<String> identifyWeapon(ItemStack stack) {
        return itemManager.getIdentityService().idOf(stack);
    }

    @Override
    public Set<String> getAllWeaponIds() {
        return itemManager.getAllWeapons().keySet();
    }

    @Override
    public boolean weaponMeetsRequirements(UUID playerId, String weaponId) {
        return itemManager.findById(weaponId)
                .map(data -> itemManager.getRequirementService().meetsRequirements(playerId, data))
                .orElse(false);
    }

    @Override
    public int getEnhancementLevel(ItemStack stack) {
        return itemManager.getIdentityService().getEnhancementLevel(stack);
    }

    @Override
    public int enhanceWeapon(ItemStack stack) {
        return itemManager.getIdentityService().enhance(stack);
    }
}
