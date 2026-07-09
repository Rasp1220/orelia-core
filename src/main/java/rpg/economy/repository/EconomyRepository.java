package rpg.economy.repository;

import rpg.database.manager.DatabaseManager;
import rpg.database.repository.SchemaOwner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Direct DB-backed balance storage. Unlike most modules, economy reads/writes go straight
 * to the database on every call instead of going through Core's online-player cache,
 * because Vault's {@code Economy} interface must also answer for offline players.
 */
public final class EconomyRepository implements SchemaOwner {

    private final DatabaseManager databaseManager;
    private final double startingBalance;

    public EconomyRepository(DatabaseManager databaseManager, double startingBalance) {
        this.databaseManager = databaseManager;
        this.startingBalance = startingBalance;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_economy (
                        uuid VARCHAR(36) PRIMARY KEY,
                        balance DOUBLE NOT NULL DEFAULT 0
                    )
                    """);
        }
    }

    public boolean hasAccount(UUID uuid) {
        String sql = "SELECT 1 FROM player_economy WHERE uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check economy account for " + uuid, e);
        }
    }

    public void createAccountIfMissing(UUID uuid) {
        if (hasAccount(uuid)) {
            return;
        }
        setBalance(uuid, startingBalance);
    }

    public double getBalance(UUID uuid) {
        String sql = "SELECT balance FROM player_economy WHERE uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read balance for " + uuid, e);
        }
        createAccountIfMissing(uuid);
        return startingBalance;
    }

    public void setBalance(UUID uuid, double balance) {
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO player_economy (uuid, balance) VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance
                    """;
            case MYSQL -> """
                    INSERT INTO player_economy (uuid, balance) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE balance = VALUES(balance)
                    """;
        };
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, Math.max(0, balance));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to set balance for " + uuid, e);
        }
    }
}
