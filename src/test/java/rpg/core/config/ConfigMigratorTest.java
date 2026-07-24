package rpg.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigMigratorTest {

    private static final Logger LOGGER = Logger.getLogger(ConfigMigratorTest.class.getName());

    @Test
    void appendsMissingTopLevelSectionFromBundledDefault(@TempDir Path tempDir) throws IOException {
        String existingText = """
                config-version: 1

                # Existing section.
                database:
                  type: SQLITE
                """;
        String bundledText = """
                config-version: 2

                # Existing section.
                database:
                  type: SQLITE

                # A brand new section added in version 2.
                new-feature:
                  enabled: true
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(result.contains("# A brand new section added in version 2."));
        assertTrue(result.contains("new-feature:"));
        assertTrue(result.contains("enabled: true"));
        // Existing content is untouched, not reformatted/reordered.
        assertTrue(result.startsWith(existingText));
    }

    @Test
    void doesNothingWhenExistingVersionIsAlreadyCurrent(@TempDir Path tempDir) throws IOException {
        String existingText = """
                config-version: 2

                database:
                  type: SQLITE
                """;
        String bundledText = """
                config-version: 2

                database:
                  type: SQLITE

                new-feature:
                  enabled: true
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertFalse(result.contains("new-feature"));
    }

    @Test
    void appendsMissingKeyInsideExistingTopLevelSection(@TempDir Path tempDir) throws IOException {
        String existingText = """
                config-version: 1

                quest:
                  max-active: 20
                """;
        String bundledText = """
                config-version: 2

                quest:
                  max-active: 20
                  # Cooldown in seconds before a repeatable quest can be accepted again.
                  on-cooldown: 3600
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(result.contains("max-active: 20"));
        assertTrue(result.contains("  # Cooldown in seconds before a repeatable quest can be accepted again."));
        assertTrue(result.contains("  on-cooldown: 3600"));
        // The new key must land inside the existing `quest:` section, not appended after it as a
        // sibling - i.e. it must appear before the section ends, not merely somewhere in the file.
        int questIndex = result.indexOf("quest:");
        int cooldownIndex = result.indexOf("on-cooldown: 3600");
        assertTrue(cooldownIndex > questIndex);
    }

    @Test
    void appendsMissingKeyTwoLevelsDeepInsideExistingTree(@TempDir Path tempDir) throws IOException {
        String existingText = """
                config-version: 1

                status:
                  growth:
                    HP:
                      base: 100
                """;
        String bundledText = """
                config-version: 2

                status:
                  growth:
                    HP:
                      base: 100
                      per-level: 12
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(result.contains("      per-level: 12"));
        // Untouched existing nesting stays exactly as-is.
        assertTrue(result.contains("status:\n  growth:\n    HP:\n      base: 100"));
    }

    @Test
    void appendsWholeNewNestedSectionAtOnceWhenParentDoesNotExist(@TempDir Path tempDir) throws IOException {
        String existingText = """
                config-version: 1

                status:
                  growth:
                    HP:
                      base: 100
                """;
        String bundledText = """
                config-version: 2

                status:
                  growth:
                    HP:
                      base: 100
                    MP:
                      base: 50
                      per-level: 5
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(result.contains("MP:"));
        assertTrue(result.contains("base: 50"));
        assertTrue(result.contains("per-level: 5"));
    }

    @Test
    void migratingTwiceDoesNotDuplicateAppendedKeys(@TempDir Path tempDir) throws IOException {
        String existingText = """
                config-version: 1

                quest:
                  max-active: 20
                """;
        String bundledText = """
                config-version: 2

                quest:
                  max-active: 20
                  on-cooldown: 3600
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);
        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        int firstIndex = result.indexOf("on-cooldown: 3600");
        int lastIndex = result.lastIndexOf("on-cooldown: 3600");
        assertEquals(firstIndex, lastIndex);
    }

    @Test
    void backfillsMissingConfigVersionAtTopOfFile(@TempDir Path tempDir) throws IOException {
        String existingText = """
                database:
                  type: SQLITE
                """;
        String bundledText = """
                config-version: 1

                database:
                  type: SQLITE
                """;

        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, existingText, StandardCharsets.UTF_8);

        ConfigMigrator.migrate(LOGGER, file.toFile(), bundledText);

        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(result.startsWith("config-version: 1"));
        assertTrue(result.contains("type: SQLITE"));
    }
}
