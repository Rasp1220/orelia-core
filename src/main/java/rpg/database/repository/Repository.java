package rpg.database.repository;

import java.util.Optional;

/**
 * Minimal CRUD contract implemented by every module's {@code repository} package.
 * Repositories only talk to the database (via {@link rpg.database.manager.DatabaseManager})
 * or a config file - never to Bukkit events or game logic.
 *
 * @param <K> primary key type
 * @param <V> stored value type
 */
public interface Repository<K, V> {

    Optional<V> findById(K id);

    void save(V value);

    void delete(K id);
}
