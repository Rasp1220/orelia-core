package rpg.boss;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import rpg.boss.listener.BossEncounterListener;
import rpg.boss.listener.BossEnrageListener;
import rpg.boss.manager.BossStateManager;
import rpg.boss.repository.BossRepository;
import rpg.core.OreliaPlugin;
import rpg.core.module.RpgModule;
import rpg.monster.MonsterModule;

import java.util.Optional;

/**
 * Boss module: config-driven boss definitions (bosses.yml) layered on top of an existing
 * monster entry, adding HP-threshold phases and an enrage damage multiplier.
 */
public final class BossModule implements RpgModule {

    private final BossRepository repository = new BossRepository();
    private final BossStateManager stateManager = new BossStateManager();
    private MonsterModule monsterModule;
    private OreliaPlugin plugin;

    @Override
    public String getName() {
        return "boss";
    }

    @Override
    public void onEnable(OreliaPlugin plugin) {
        this.plugin = plugin;
        this.monsterModule = plugin.getModuleManager().get(MonsterModule.class)
                .orElseThrow(() -> new IllegalStateException("boss module requires monster module"));

        reloadBosses();

        plugin.getServer().getPluginManager().registerEvents(
                new BossEncounterListener(monsterModule.getSpawnService(), repository, stateManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(
                new BossEnrageListener(monsterModule.getSpawnService(), repository, stateManager), plugin);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadBosses();
    }

    private void reloadBosses() {
        plugin.getConfigManager().register("bosses.yml");
        YamlConfiguration config = plugin.getConfigManager().get("bosses.yml").get();
        repository.load(config);
    }

    public Optional<LivingEntity> spawn(String bossId, Location location) {
        return repository.findById(bossId).flatMap(boss -> monsterModule.getSpawnService().spawn(boss.getMonsterId(), location));
    }

    public BossRepository getRepository() {
        return repository;
    }
}
