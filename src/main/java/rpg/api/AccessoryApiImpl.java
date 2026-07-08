package rpg.api;

import org.bukkit.inventory.ItemStack;
import rpg.accessory.repository.AccessoryRepository;
import rpg.accessory.service.AccessoryFactory;

import java.util.Optional;
import java.util.Set;

final class AccessoryApiImpl implements AccessoryApi {

    private final AccessoryRepository repository;
    private final AccessoryFactory factory;

    AccessoryApiImpl(AccessoryRepository repository, AccessoryFactory factory) {
        this.repository = repository;
        this.factory = factory;
    }

    @Override
    public Optional<ItemStack> createAccessory(String accessoryId) {
        return repository.findById(accessoryId).map(factory::create);
    }

    @Override
    public Set<String> getAllAccessoryIds() {
        return repository.getAll().keySet();
    }
}
