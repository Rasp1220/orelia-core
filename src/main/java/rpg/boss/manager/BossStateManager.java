package rpg.boss.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of {@link BossRuntimeState} keyed by the spawned boss entity's UUID.
 */
public final class BossStateManager {

    private final Map<UUID, BossRuntimeState> states = new ConcurrentHashMap<>();

    public BossRuntimeState stateOf(UUID entityId) {
        return states.computeIfAbsent(entityId, id -> new BossRuntimeState());
    }

    public void clear(UUID entityId) {
        states.remove(entityId);
    }
}
