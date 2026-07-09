package rpg.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Cross-plugin surface over the job module.
 */
public interface JobApi {

    /** Current job name (e.g. {@code "SWORDSMAN"}), or empty if unemployed/not loaded. */
    Optional<String> getCurrentJob(UUID playerId);

    boolean canUseWeaponType(UUID playerId, String weaponType);

    /** Returns false if {@code jobName} has no matching jobs.yml definition. */
    boolean changeJob(UUID playerId, String jobName);
}
