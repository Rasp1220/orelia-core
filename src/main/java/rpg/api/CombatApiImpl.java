package rpg.api;

import org.bukkit.entity.LivingEntity;
import rpg.boss.model.BossData;
import rpg.boss.repository.BossRepository;
import rpg.monster.service.MonsterSpawnService;

import java.util.Optional;

final class CombatApiImpl implements CombatApi {

    private final MonsterSpawnService monsterSpawnService;
    private final BossRepository bossRepository;

    CombatApiImpl(MonsterSpawnService monsterSpawnService, BossRepository bossRepository) {
        this.monsterSpawnService = monsterSpawnService;
        this.bossRepository = bossRepository;
    }

    @Override
    public Optional<String> identifyMonster(LivingEntity entity) {
        return monsterSpawnService.idOf(entity);
    }

    @Override
    public Optional<String> identifyBoss(String monsterId) {
        return bossRepository.findByMonsterId(monsterId).map(BossData::getId);
    }
}
