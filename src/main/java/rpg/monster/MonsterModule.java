package rpg.monster;

import org.bukkit.configuration.file.YamlConfiguration;
import rpg.core.OreliaPlugin;
import rpg.core.module.RpgModule;
import rpg.economy.EconomyModule;
import rpg.item.ItemModule;
import rpg.monster.listener.MonsterCombatListener;
import rpg.monster.listener.MonsterDeathListener;
import rpg.monster.repository.MonsterRepository;
import rpg.monster.service.MonsterDropService;
import rpg.monster.service.MonsterKeys;
import rpg.monster.service.MonsterSpawnService;
import rpg.status.StatusModule;

/**
 * Monster module: config-driven monster templates (monsters.yml), spawning, combat stat
 * application, and drop/EXP/money rewards on death.
 */
public final class MonsterModule implements RpgModule {

    private final MonsterRepository repository = new MonsterRepository();
    private MonsterSpawnService spawnService;
    private OreliaPlugin plugin;

    @Override
    public String getName() {
        return "monster";
    }

    @Override
    public void onEnable(OreliaPlugin plugin) {
        this.plugin = plugin;
        ItemModule itemModule = plugin.getModuleManager().get(ItemModule.class)
                .orElseThrow(() -> new IllegalStateException("monster module requires item module"));
        EconomyModule economyModule = plugin.getModuleManager().get(EconomyModule.class)
                .orElseThrow(() -> new IllegalStateException("monster module requires economy module"));
        StatusModule statusModule = plugin.getModuleManager().get(StatusModule.class)
                .orElseThrow(() -> new IllegalStateException("monster module requires status module"));

        reloadMonsters();

        MonsterKeys keys = new MonsterKeys(plugin);
        this.spawnService = new MonsterSpawnService(keys, repository);
        MonsterDropService dropService = new MonsterDropService(
                itemModule.getItemManager(), economyModule.getEconomyService(), statusModule.getStatusService());

        plugin.getServer().getPluginManager().registerEvents(new MonsterCombatListener(spawnService), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MonsterDeathListener(spawnService, dropService), plugin);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadMonsters();
    }

    private void reloadMonsters() {
        plugin.getConfigManager().register("monsters.yml");
        YamlConfiguration config = plugin.getConfigManager().get("monsters.yml").get();
        repository.load(config);
    }

    public MonsterRepository getRepository() {
        return repository;
    }

    public MonsterSpawnService getSpawnService() {
        return spawnService;
    }
}
