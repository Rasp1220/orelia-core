package rpg.status.manager;

import rpg.core.player.PlayerDataComponentLoader;
import rpg.status.model.PlayerStatusComponent;
import rpg.status.repository.StatusRepository;

import java.util.UUID;

/**
 * Bridges {@link StatusRepository} to Core's player data lifecycle. This is the only
 * class Core's {@code PlayerDataManager} interacts with for status data.
 */
public final class StatusManager implements PlayerDataComponentLoader<PlayerStatusComponent> {

    private final StatusRepository repository;

    public StatusManager(StatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public Class<PlayerStatusComponent> type() {
        return PlayerStatusComponent.class;
    }

    @Override
    public PlayerStatusComponent loadOrCreate(UUID uuid) {
        return repository.loadOrCreate(uuid);
    }

    @Override
    public void save(PlayerStatusComponent component) {
        repository.save(component);
    }
}
