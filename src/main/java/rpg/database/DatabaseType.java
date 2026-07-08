package rpg.database;

/**
 * Supported storage backends, selected via {@code config.yml: database.type}.
 */
public enum DatabaseType {
    SQLITE,
    MYSQL;

    public static DatabaseType parse(String raw, DatabaseType fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return DatabaseType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
