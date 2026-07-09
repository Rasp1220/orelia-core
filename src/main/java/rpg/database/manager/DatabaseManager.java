package rpg.database.manager;

import rpg.database.DatabaseType;
import rpg.database.connection.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Facade every module repository depends on to obtain a {@link Connection}. Owns no
 * table schemas itself - each module's repository creates and migrates its own tables
 * on top of this connection, keeping data-access ownership with the module that needs it.
 */
public final class DatabaseManager {

    private final DatabaseType type;
    private final ConnectionProvider connectionProvider;

    public DatabaseManager(DatabaseType type, ConnectionProvider connectionProvider) {
        this.type = type;
        this.connectionProvider = connectionProvider;
    }

    public Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }

    public DatabaseType getType() {
        return type;
    }

    public void shutdown() {
        connectionProvider.close();
    }
}
