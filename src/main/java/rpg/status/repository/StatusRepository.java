package rpg.status.repository;

import rpg.database.manager.DatabaseManager;
import rpg.database.repository.SchemaOwner;
import rpg.status.model.PlayerStatusComponent;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;
import rpg.status.service.LevelGrowthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Persists the parts of {@link PlayerStatusComponent} that must survive a restart:
 * level, experience, current HP/MP. Equipment contributions and buffs are runtime-only
 * and are rebuilt by the item/accessory/skill modules on join.
 */
public final class StatusRepository implements SchemaOwner {

    private final DatabaseManager databaseManager;
    private final LevelGrowthService levelGrowthService;

    public StatusRepository(DatabaseManager databaseManager, LevelGrowthService levelGrowthService) {
        this.databaseManager = databaseManager;
        this.levelGrowthService = levelGrowthService;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_status (
                        uuid VARCHAR(36) PRIMARY KEY,
                        level INTEGER NOT NULL DEFAULT 1,
                        experience BIGINT NOT NULL DEFAULT 0,
                        current_hp DOUBLE NOT NULL DEFAULT 0,
                        current_mp DOUBLE NOT NULL DEFAULT 0
                    )
                    """);
        }
    }

    public PlayerStatusComponent loadOrCreate(UUID uuid) {
        String sql = "SELECT level, experience, current_hp, current_mp FROM player_status WHERE uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int level = resultSet.getInt("level");
                    StatSheet baseStats = levelGrowthService.baseStatsForLevel(level);
                    return new PlayerStatusComponent(
                            uuid,
                            level,
                            resultSet.getLong("experience"),
                            baseStats,
                            resultSet.getDouble("current_hp"),
                            resultSet.getDouble("current_mp"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load status for " + uuid, e);
        }

        StatSheet baseStats = levelGrowthService.baseStatsForLevel(1);
        return new PlayerStatusComponent(uuid, 1, 0L, baseStats, baseStats.get(StatType.HP), baseStats.get(StatType.MP));
    }

    public void save(PlayerStatusComponent component) {
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO player_status (uuid, level, experience, current_hp, current_mp) VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET level = excluded.level, experience = excluded.experience,
                        current_hp = excluded.current_hp, current_mp = excluded.current_mp
                    """;
            case MYSQL -> """
                    INSERT INTO player_status (uuid, level, experience, current_hp, current_mp) VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE level = VALUES(level), experience = VALUES(experience),
                        current_hp = VALUES(current_hp), current_mp = VALUES(current_mp)
                    """;
        };
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, component.getOwner().toString());
            statement.setInt(2, component.getLevel());
            statement.setLong(3, component.getExperience());
            statement.setDouble(4, component.getCurrentHp());
            statement.setDouble(5, component.getCurrentMp());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save status for " + component.getOwner(), e);
        }
    }
}
