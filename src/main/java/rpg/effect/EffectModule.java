package rpg.effect;

import org.bukkit.configuration.file.YamlConfiguration;
import rpg.core.OreliaPlugin;
import rpg.core.module.RpgModule;
import rpg.effect.repository.EffectRepository;
import rpg.effect.service.EffectPlaybackService;

/**
 * Effect module: config-driven particle/sound bundles (effects.yml) usable by any other
 * core module (skill executors, monster/boss abilities) and, through the API, by
 * orelia-world's CutSceneModule.
 */
public final class EffectModule implements RpgModule {

    private final EffectRepository repository = new EffectRepository();
    private EffectPlaybackService playbackService;
    private OreliaPlugin plugin;

    @Override
    public String getName() {
        return "effect";
    }

    @Override
    public void onEnable(OreliaPlugin plugin) {
        this.plugin = plugin;
        this.playbackService = new EffectPlaybackService(repository);
        reloadEffects();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadEffects();
    }

    private void reloadEffects() {
        plugin.getConfigManager().register("effects.yml");
        YamlConfiguration config = plugin.getConfigManager().get("effects.yml").get();
        repository.load(config);
    }

    public EffectPlaybackService getPlaybackService() {
        return playbackService;
    }

    public EffectRepository getRepository() {
        return repository;
    }
}
