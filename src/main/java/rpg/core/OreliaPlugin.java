package rpg.core;

import org.bukkit.plugin.java.JavaPlugin;
import rpg.core.command.AdminCommand;
import rpg.core.config.ConfigManager;
import rpg.core.listener.PlayerConnectionListener;
import rpg.core.module.ModuleManager;
import rpg.core.player.PlayerDataManager;
import rpg.core.scheduler.SchedulerService;
import rpg.database.DatabaseModule;
import rpg.status.StatusModule;
import rpg.job.JobModule;
import rpg.item.ItemModule;
import rpg.skill.SkillModule;
import rpg.accessory.AccessoryModule;
import rpg.effect.EffectModule;
import rpg.economy.EconomyModule;
import rpg.monster.MonsterModule;
import rpg.boss.BossModule;
import rpg.gui.GuiModule;
import rpg.api.ApiModule;

/**
 * Plugin entry point for the orelia-core repo/jar. Owns process-wide singletons (config,
 * player data, scheduler, module registry) and wires every top-level Module in dependency
 * order. No gameplay logic lives here; see the individual module packages.
 *
 * <p>orelia-world and orelia-extra are separate plugins/jars built from separate repos.
 * They depend on this plugin ({@code depend: [OreliaCore]} in their own plugin.yml) and
 * talk to it only through {@link rpg.api}, published via Bukkit's {@code ServicesManager} -
 * never by reaching into these module classes directly.
 */
public final class OreliaPlugin extends JavaPlugin {

    private static OreliaPlugin instance;

    private ConfigManager configManager;
    private SchedulerService schedulerService;
    private PlayerDataManager playerDataManager;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.register("config.yml");

        this.schedulerService = new SchedulerService(this);
        this.playerDataManager = new PlayerDataManager(getLogger(), schedulerService);
        this.moduleManager = new ModuleManager(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(playerDataManager), this);
        getCommand("rpgadmin").setExecutor(new AdminCommand(this));

        // Registration order doubles as dependency order: later modules may look up
        // earlier ones via ModuleManager#get, never the reverse. ApiModule is always last
        // so every service it publishes is fully constructed first.
        moduleManager.register(new DatabaseModule());
        moduleManager.register(new StatusModule());
        moduleManager.register(new JobModule());
        moduleManager.register(new ItemModule());
        moduleManager.register(new SkillModule());
        moduleManager.register(new AccessoryModule());
        moduleManager.register(new EffectModule());
        moduleManager.register(new EconomyModule());
        moduleManager.register(new MonsterModule());
        moduleManager.register(new BossModule());
        moduleManager.register(new GuiModule());
        moduleManager.register(new ApiModule());

        moduleManager.enableAll();
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAllOnlineSync();
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        instance = null;
    }

    public void reload() {
        configManager.reloadAll();
        moduleManager.reloadAll();
    }

    public static OreliaPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
