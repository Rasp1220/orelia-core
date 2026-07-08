package rpg.skill.repository;

import rpg.database.manager.DatabaseManager;
import rpg.database.repository.SchemaOwner;
import rpg.skill.model.PlayerSkillComponent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists skill points and per-skill levels. Cooldowns are intentionally not stored -
 * they are runtime-only and reset when a player rejoins.
 */
public final class PlayerSkillRepository implements SchemaOwner {

    private final DatabaseManager databaseManager;

    public PlayerSkillRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_skill_points (
                        uuid VARCHAR(36) PRIMARY KEY,
                        points INTEGER NOT NULL DEFAULT 0
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_skill_level (
                        uuid VARCHAR(36) NOT NULL,
                        skill_id VARCHAR(64) NOT NULL,
                        level INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (uuid, skill_id)
                    )
                    """);
        }
    }

    public PlayerSkillComponent loadOrCreate(UUID uuid) {
        int points = 0;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT points FROM player_skill_points WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    points = resultSet.getInt("points");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load skill points for " + uuid, e);
        }

        Map<String, Integer> levels = new HashMap<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT skill_id, level FROM player_skill_level WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    levels.put(resultSet.getString("skill_id"), resultSet.getInt("level"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load skill levels for " + uuid, e);
        }

        return new PlayerSkillComponent(uuid, points, levels);
    }

    public void save(PlayerSkillComponent component) {
        String pointsSql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO player_skill_points (uuid, points) VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET points = excluded.points
                    """;
            case MYSQL -> """
                    INSERT INTO player_skill_points (uuid, points) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE points = VALUES(points)
                    """;
        };
        String levelSql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO player_skill_level (uuid, skill_id, level) VALUES (?, ?, ?)
                    ON CONFLICT(uuid, skill_id) DO UPDATE SET level = excluded.level
                    """;
            case MYSQL -> """
                    INSERT INTO player_skill_level (uuid, skill_id, level) VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE level = VALUES(level)
                    """;
        };

        try (Connection connection = databaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(pointsSql)) {
                statement.setString(1, component.getOwner().toString());
                statement.setInt(2, component.getSkillPoints());
                statement.executeUpdate();
            }
            for (Map.Entry<String, Integer> entry : component.getSkillLevels().entrySet()) {
                try (PreparedStatement statement = connection.prepareStatement(levelSql)) {
                    statement.setString(1, component.getOwner().toString());
                    statement.setString(2, entry.getKey());
                    statement.setInt(3, entry.getValue());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save skills for " + component.getOwner(), e);
        }
    }
}
