package rpg.job.repository;

import rpg.database.manager.DatabaseManager;
import rpg.database.repository.SchemaOwner;
import rpg.job.model.JobType;
import rpg.job.model.PlayerJobComponent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Persists the player's currently selected job.
 */
public final class PlayerJobRepository implements SchemaOwner {

    private final DatabaseManager databaseManager;

    public PlayerJobRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_job (
                        uuid VARCHAR(36) PRIMARY KEY,
                        job VARCHAR(32)
                    )
                    """);
        }
    }

    public PlayerJobComponent loadOrCreate(UUID uuid) {
        String sql = "SELECT job FROM player_job WHERE uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String job = resultSet.getString("job");
                    return new PlayerJobComponent(uuid, job == null ? null : JobType.valueOf(job));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load job for " + uuid, e);
        }
        return new PlayerJobComponent(uuid, null);
    }

    public void save(PlayerJobComponent component) {
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO player_job (uuid, job) VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET job = excluded.job
                    """;
            case MYSQL -> """
                    INSERT INTO player_job (uuid, job) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE job = VALUES(job)
                    """;
        };
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, component.getOwner().toString());
            JobType job = component.getCurrentJob();
            statement.setString(2, job == null ? null : job.name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save job for " + component.getOwner(), e);
        }
    }
}
