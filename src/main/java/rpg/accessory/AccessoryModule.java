package rpg.accessory;

import org.bukkit.configuration.file.YamlConfiguration;
import rpg.accessory.listener.AccessorySlotListener;
import rpg.accessory.repository.AccessoryRepository;
import rpg.accessory.service.AccessoryEffectService;
import rpg.accessory.service.AccessoryFactory;
import rpg.accessory.service.AccessoryIdentityService;
import rpg.accessory.service.AccessoryKeys;
import rpg.core.OreliaPlugin;
import rpg.core.module.RpgModule;
import rpg.status.StatusModule;

/**
 * Accessory module: dedicated bottom-row inventory slots (charm/ring/necklace/wing) whose
 * stat bonus only applies while the matching item sits in its designated slot.
 */
public final class AccessoryModule implements RpgModule {

    private final AccessoryRepository repository = new AccessoryRepository();
    private AccessoryFactory factory;
    private AccessoryIdentityService identityService;
    private OreliaPlugin plugin;

    @Override
    public String getName() {
        return "accessory";
    }

    @Override
    public void onEnable(OreliaPlugin plugin) {
        this.plugin = plugin;
        StatusModule statusModule = plugin.getModuleManager().get(StatusModule.class)
                .orElseThrow(() -> new IllegalStateException("accessory module requires status module"));

        reloadAccessories();

        AccessoryKeys keys = new AccessoryKeys(plugin);
        this.factory = new AccessoryFactory(keys);
        this.identityService = new AccessoryIdentityService(keys, repository);
        AccessoryEffectService effectService = new AccessoryEffectService(statusModule.getStatusService(), identityService);

        plugin.getServer().getPluginManager().registerEvents(
                new AccessorySlotListener(identityService, effectService, plugin.getSchedulerService()), plugin);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadAccessories();
    }

    private void reloadAccessories() {
        plugin.getConfigManager().register("accessories.yml");
        YamlConfiguration config = plugin.getConfigManager().get("accessories.yml").get();
        repository.load(config);
    }

    public AccessoryRepository getRepository() {
        return repository;
    }

    public AccessoryFactory getFactory() {
        return factory;
    }

    public AccessoryIdentityService getIdentityService() {
        return identityService;
    }
}
