package rpg.core.player;

import java.util.UUID;

/**
 * Registered by a module with {@link PlayerDataManager} so Core can load/save that
 * module's component during the player join/quit lifecycle without depending on the
 * module's package.
 *
 * @param <T> the component type this loader produces
 */
public interface PlayerDataComponentLoader<T extends PlayerDataComponent> {

    Class<T> type();

    /**
     * Called off the main thread during player join. Must never return null; create a
     * fresh default component when no saved data exists.
     */
    T loadOrCreate(UUID uuid);

    /**
     * Called off the main thread during player quit (and on periodic autosave).
     */
    void save(T component);
}
