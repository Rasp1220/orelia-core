package rpg.database.connection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite is a single-file, single-writer database, so this provider keeps one long-lived
 * connection open for the plugin's lifetime rather than pooling.
 */
public final class SQLiteConnectionProvider implements ConnectionProvider {

    private final File databaseFile;
    private Connection connection;

    public SQLiteConnectionProvider(File dataFolder, String fileName) {
        this.databaseFile = new File(dataFolder, fileName);
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (!databaseFile.getParentFile().exists()) {
                databaseFile.getParentFile().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 5000");
            }
        }
        return connection;
    }

    @Override
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
