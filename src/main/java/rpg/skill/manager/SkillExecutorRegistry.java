package rpg.skill.manager;

import rpg.skill.executor.SkillExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Maps a skills.yml {@code executor-type} string to the {@link SkillExecutor} that runs
 * it. New skills reusing an existing archetype only need a config entry; new archetypes
 * register themselves here.
 */
public final class SkillExecutorRegistry {

    private final Map<String, SkillExecutor> executors = new HashMap<>();

    public void register(String executorType, SkillExecutor executor) {
        executors.put(executorType, executor);
    }

    public Optional<SkillExecutor> get(String executorType) {
        return Optional.ofNullable(executors.get(executorType));
    }
}
