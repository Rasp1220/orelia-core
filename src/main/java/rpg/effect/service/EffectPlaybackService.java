package rpg.effect.service;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import rpg.effect.model.EffectData;
import rpg.effect.repository.EffectRepository;

/**
 * Plays a config-defined {@link EffectData} at a location or on an entity. Unknown
 * particle/sound names are ignored rather than throwing, so a typo in effects.yml never
 * takes down the skill/ability that triggered it.
 */
public final class EffectPlaybackService {

    private final EffectRepository repository;

    public EffectPlaybackService(EffectRepository repository) {
        this.repository = repository;
    }

    public void playAt(Location location, String effectId) {
        repository.findById(effectId).ifPresent(effect -> play(location, effect));
    }

    public void playOnEntity(Entity entity, String effectId) {
        playAt(entity.getLocation(), effectId);
    }

    private void play(Location location, EffectData effect) {
        if (location.getWorld() == null) {
            return;
        }
        try {
            location.getWorld().spawnParticle(Particle.valueOf(effect.getParticle()), location,
                    effect.getParticleCount(), effect.getSpreadX(), effect.getSpreadY(), effect.getSpreadZ());
        } catch (IllegalArgumentException ignored) {
        }
        if (effect.getSound() != null && !effect.getSound().isBlank()) {
            try {
                location.getWorld().playSound(location, Sound.valueOf(effect.getSound()), effect.getSoundVolume(), effect.getSoundPitch());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
