package rpg.database.repository;

import rpg.database.manager.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Owns the base {@code players} account table (UUID <-> last known name, timestamps).
 * Module-specific data (stats, job, quest log, ...) lives in each module's own tables,
 * joined back to this one by UUID.
 */
public final class PlayerAccountRepository implements SchemaOwner {

    private final DatabaseManager databaseManager;

    public PlayerAccountRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(16) NOT NULL,
                        first_join BIGINT NOT NULL,
                        last_join BIGINT NOT NULL
                    )
                    """);
        }
    }

    public void upsert(UUID uuid, String name, long nowMillis) {
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO players (uuid, name, first_join, last_join) VALUES (?, ?, ?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, last_join = excluded.last_join
                    """;
            case MYSQL -> """
                    INSERT INTO players (uuid, name, first_join, last_join) VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE name = VALUES(name), last_join = VALUES(last_join)
                    """;
        };
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setLong(3, nowMillis);
            statement.setLong(4, nowMillis);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to upsert player account: " + uuid, e);
        }
    }
}
