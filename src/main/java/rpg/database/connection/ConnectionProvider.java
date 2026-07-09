package rpg.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Supplies JDBC connections for one backend (SQLite or MySQL). Implementations decide
 * whether that means one shared connection or a pool; callers only ever see a
 * {@link Connection}.
 */
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;

    void close();
}
