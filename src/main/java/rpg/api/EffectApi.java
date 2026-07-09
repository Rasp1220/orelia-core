package rpg.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Cross-plugin surface over the effect module, primarily for orelia-world's
 * CutSceneModule to reuse core's particle/sound bundles.
 */
public interface EffectApi {

    void playAt(Location location, String effectId);

    void playOnEntity(Entity entity, String effectId);
}
