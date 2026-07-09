package rpg.skill.manager;

import rpg.core.player.PlayerDataComponentLoader;
import rpg.skill.model.PlayerSkillComponent;
import rpg.skill.repository.PlayerSkillRepository;

import java.util.UUID;

/**
 * Bridges {@link PlayerSkillRepository} to Core's player data lifecycle.
 */
public final class SkillManager implements PlayerDataComponentLoader<PlayerSkillComponent> {

    private final PlayerSkillRepository repository;

    public SkillManager(PlayerSkillRepository repository) {
        this.repository = repository;
    }

    @Override
    public Class<PlayerSkillComponent> type() {
        return PlayerSkillComponent.class;
    }

    @Override
    public PlayerSkillComponent loadOrCreate(UUID uuid) {
        return repository.loadOrCreate(uuid);
    }

    @Override
    public void save(PlayerSkillComponent component) {
        repository.save(component);
    }
}
