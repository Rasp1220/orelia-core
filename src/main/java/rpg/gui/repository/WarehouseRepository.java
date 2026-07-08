package rpg.gui.repository;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import rpg.database.manager.DatabaseManager;
import rpg.database.repository.SchemaOwner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.UUID;

/**
 * Persists each player's personal warehouse (SOW section 12 "倉庫" NPC / section 17
 * "倉庫" GUI) as a serialized {@link ItemStack} array.
 */
public final class WarehouseRepository implements SchemaOwner {

    public static final int SIZE = 54;

    private final DatabaseManager databaseManager;

    public WarehouseRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_warehouse (
                        uuid VARCHAR(36) PRIMARY KEY,
                        contents TEXT
                    )
                    """);
        }
    }

    public ItemStack[] load(UUID uuid) {
        String sql = "SELECT contents FROM player_warehouse WHERE uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String encoded = resultSet.getString("contents");
                    if (encoded != null && !encoded.isBlank()) {
                        return deserialize(encoded);
                    }
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load warehouse for " + uuid, e);
        }
        return new ItemStack[SIZE];
    }

    public void save(UUID uuid, ItemStack[] contents) {
        String encoded;
        try {
            encoded = serialize(contents);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize warehouse for " + uuid, e);
        }
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> """
                    INSERT INTO player_warehouse (uuid, contents) VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET contents = excluded.contents
                    """;
            case MYSQL -> """
                    INSERT INTO player_warehouse (uuid, contents) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE contents = VALUES(contents)
                    """;
        };
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, encoded);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save warehouse for " + uuid, e);
        }
    }

    private String serialize(ItemStack[] contents) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream dataStream = new BukkitObjectOutputStream(byteStream)) {
            dataStream.writeInt(contents.length);
            for (ItemStack stack : contents) {
                dataStream.writeObject(stack);
            }
        }
        return Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }

    private ItemStack[] deserialize(String encoded) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
        try (BukkitObjectInputStream dataStream = new BukkitObjectInputStream(byteStream)) {
            int length = dataStream.readInt();
            ItemStack[] contents = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                contents[i] = (ItemStack) dataStream.readObject();
            }
            return contents;
        }
    }
}
