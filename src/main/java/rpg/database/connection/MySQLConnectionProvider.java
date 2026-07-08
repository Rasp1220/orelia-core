package rpg.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a JDBC connection per {@link #getConnection()} call, relying on MySQL Connector/J's
 * own internal reconnect handling. For high player counts, swap this out for a real pool
 * (e.g. HikariCP) behind the same {@link ConnectionProvider} interface.
 */
public final class MySQLConnectionProvider implements ConnectionProvider {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public MySQLConnectionProvider(String host, int port, String database, String username, String password, boolean useSsl) {
        this.jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSsl + "&autoReconnect=true&characterEncoding=UTF-8";
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void close() {
        // No pool to release; each connection is closed by its caller.
    }
}
