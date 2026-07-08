package rpg.core.player;

import java.util.UUID;

/**
 * Marker for a module-owned slice of per-player state (e.g. status stats, job progress,
 * quest log). Core stores components by type inside {@link PlayerData} without knowing
 * their contents; only the owning module reads/writes the fields.
 */
public interface PlayerDataComponent {

    UUID getOwner();
}
