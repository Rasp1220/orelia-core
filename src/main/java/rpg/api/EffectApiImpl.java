package rpg.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import rpg.effect.service.EffectPlaybackService;

final class EffectApiImpl implements EffectApi {

    private final EffectPlaybackService playbackService;

    EffectApiImpl(EffectPlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @Override
    public void playAt(Location location, String effectId) {
        playbackService.playAt(location, effectId);
    }

    @Override
    public void playOnEntity(Entity entity, String effectId) {
        playbackService.playOnEntity(entity, effectId);
    }
}
