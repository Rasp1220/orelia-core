package rpg.core.module;

import rpg.core.OreliaPlugin;

/**
 * Lifecycle contract implemented by every top-level Module (item, skill, job, ...).
 * A module owns exactly one area of game logic and must not reach into another
 * module's internals directly; cross-module data flows through {@code core.player}
 * components or the {@code api} module.
 */
public interface RpgModule {

    /**
     * Unique, human-readable module identifier (e.g. {@code "item"}, {@code "skill"}).
     */
    String getName();

    /**
     * Called once during plugin startup, in registration order.
     */
    void onEnable(OreliaPlugin plugin);

    /**
     * Called once during plugin shutdown, in reverse registration order.
     */
    void onDisable();

    /**
     * Called when an operator requests a config reload. Modules should re-read their
     * config files and rebuild in-memory repositories without requiring a server restart.
     */
    default void onReload() {
    }
}
