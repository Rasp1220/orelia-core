package rpg.database.repository;

import java.sql.SQLException;

/**
 * Implemented by repositories that own one or more SQL tables. Called once per repository
 * during its module's {@code onEnable}, after {@link rpg.database.manager.DatabaseManager}
 * is available.
 */
public interface SchemaOwner {

    void createSchemaIfNotExists() throws SQLException;
}
