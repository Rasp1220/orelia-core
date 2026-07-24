package rpg.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Adds newly-introduced config keys to an existing user config file without touching anything
 * else in it - including keys nested inside a section the user already has, not just brand new
 * top-level sections. Deliberately never loads-then-resaves the user's file through
 * {@link YamlConfiguration} - that would strip every comment, which this codebase's config
 * files rely on heavily. Instead, missing keys are spliced in as raw text (comments included,
 * copied verbatim from the bundled default) at the correct position in the existing file, and
 * keys the user has that the bundled default no longer defines are only logged as a warning,
 * never removed automatically.
 *
 * <p>Gated by a {@code config-version} integer at the top of the file: migration only runs
 * when the bundled resource's version is higher than the user's file's (missing = version 0),
 * so an up-to-date file is never re-scanned on every startup. If the existing file has no
 * {@code config-version} line at all (a file that's never had one before), the bundled
 * default's version line is backfilled at the very top of the file, as its own splice.
 *
 * <p>Structure is derived two ways in lockstep: {@link YamlConfiguration}'s parsed
 * {@link ConfigurationSection} tree decides *whether* a key is a section (recurse into it) or a
 * leaf (list/scalar - left untouched if present on both sides, since diffing list contents or
 * multi-line scalars isn't attempted), while a separate indentation-aware text scan
 * ({@link #parseBlocks}) locates the exact line range (including any directly-preceding
 * {@code #} comment lines) that must be copied or recursed into. A key that's a section on one
 * side and a leaf on the other is left untouched, same conservative behavior as a renamed key -
 * manual intervention required. Flow-style YAML and anchors/aliases aren't handled (unused in
 * this codebase's config files).
 */
final class ConfigMigrator {

    private static final Pattern KEY_LINE = Pattern.compile("^[A-Za-z0-9_.-]+:.*");

    private ConfigMigrator() {
    }

    static void migrate(Logger logger, File file, String bundledText) {
        YamlConfiguration bundled = YamlConfiguration.loadConfiguration(new StringReader(bundledText));
        YamlConfiguration existing = YamlConfiguration.loadConfiguration(file);

        int bundledVersion = bundled.getInt("config-version", 0);
        int existingVersion = existing.getInt("config-version", 0);
        if (existingVersion >= bundledVersion) {
            return;
        }

        String existingText;
        try {
            existingText = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to read " + file.getName() + " for migration", e);
            return;
        }

        String[] bundledLines = bundledText.split("\n", -1);
        String[] existingLines = existingText.split("\n", -1);

        List<Block> bundledBlocks = parseBlocks(bundledLines, 0, bundledLines.length, 0);
        List<Block> existingBlocks = parseBlocks(existingLines, 0, existingLines.length, 0);

        Map<Integer, List<String>> spliceLines = new LinkedHashMap<>();

        if (!existing.isSet("config-version") && bundled.isSet("config-version")) {
            Block versionBlock = toMap(bundledBlocks).get("config-version");
            if (versionBlock != null) {
                spliceLines.computeIfAbsent(0, k -> new ArrayList<>())
                        .addAll(sliceLines(bundledLines, versionBlock.startLine(), versionBlock.endLine()));
            }
        }

        diffSection(bundled, existing, bundledBlocks, toMap(existingBlocks), existingLines.length,
                bundledLines, spliceLines, true);

        for (String key : existing.getKeys(false)) {
            if (!bundled.getKeys(false).contains(key)) {
                logger.warning("Config key '" + key + "' in " + file.getName() + " is no longer used by this version - you can remove it.");
            }
        }

        if (spliceLines.isEmpty()) {
            return;
        }

        List<String> resultLines = new ArrayList<>(Arrays.asList(existingLines));
        List<Integer> points = new ArrayList<>(spliceLines.keySet());
        points.sort(Comparator.reverseOrder());
        for (int point : points) {
            resultLines.addAll(point, spliceLines.get(point));
        }

        try {
            Files.write(file.toPath(), String.join("\n", resultLines).getBytes(StandardCharsets.UTF_8));
            logger.info("Added new default config keys to " + file.getName() + " - see the file for details.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write migrated config to " + file.getName(), e);
        }
    }

    private static void diffSection(ConfigurationSection bundledSection, ConfigurationSection existingSection,
            List<Block> bundledBlocks, Map<String, Block> existingBlocksByKey, int insertionPoint,
            String[] bundledLines, Map<Integer, List<String>> spliceLines, boolean topLevel) {
        Map<String, Block> bundledBlocksByKey = toMap(bundledBlocks);
        for (String key : bundledSection.getKeys(false)) {
            if (topLevel && key.equals("config-version")) {
                continue;
            }
            Block bundledBlock = bundledBlocksByKey.get(key);
            if (bundledBlock == null) {
                continue;
            }
            Block existingBlock = existingBlocksByKey.get(key);
            if (existingBlock == null) {
                List<String> lines = spliceLines.computeIfAbsent(insertionPoint, k -> new ArrayList<>());
                lines.add("");
                lines.addAll(sliceLines(bundledLines, bundledBlock.startLine(), bundledBlock.endLine()));
                continue;
            }
            boolean bundledIsSection = bundledSection.isConfigurationSection(key);
            boolean existingIsSection = existingSection.isConfigurationSection(key);
            if (bundledIsSection && existingIsSection) {
                diffSection(bundledSection.getConfigurationSection(key), existingSection.getConfigurationSection(key),
                        bundledBlock.children(), toMap(existingBlock.children()), existingBlock.endLine(),
                        bundledLines, spliceLines, false);
            }
        }
    }

    /**
     * Finds every key line at exactly {@code indent} within {@code [start, end)}, claims any
     * directly-preceding same-indent comment lines for it, and recurses into its content range
     * to find children (if the first non-blank line there sits at a deeper indent).
     */
    private static List<Block> parseBlocks(String[] lines, int start, int end, int indent) {
        List<Integer> keyLines = new ArrayList<>();
        for (int i = start; i < end; i++) {
            String line = lines[i];
            if (line.isBlank() || leadingSpaces(line) != indent) {
                continue;
            }
            if (KEY_LINE.matcher(line.substring(indent)).matches()) {
                keyLines.add(i);
            }
        }
        if (keyLines.isEmpty()) {
            return List.of();
        }

        int[] startLines = new int[keyLines.size()];
        for (int m = 0; m < keyLines.size(); m++) {
            int keyLine = keyLines.get(m);
            int lowerBound = (m > 0) ? keyLines.get(m - 1) + 1 : start;
            int blockStart = keyLine;
            for (int c = keyLine - 1; c >= lowerBound; c--) {
                String commentLine = lines[c];
                if (commentLine.isBlank() || leadingSpaces(commentLine) != indent
                        || !commentLine.substring(indent).startsWith("#")) {
                    break;
                }
                blockStart = c;
            }
            startLines[m] = blockStart;
        }

        List<Block> blocks = new ArrayList<>();
        for (int m = 0; m < keyLines.size(); m++) {
            int keyLine = keyLines.get(m);
            int blockEnd = (m + 1 < keyLines.size()) ? startLines[m + 1] : end;
            String stripped = lines[keyLine].substring(indent);
            String key = stripped.substring(0, stripped.indexOf(':'));
            int childIndent = detectChildIndent(lines, keyLine + 1, blockEnd, indent);
            List<Block> children = childIndent > indent
                    ? parseBlocks(lines, keyLine + 1, blockEnd, childIndent)
                    : List.of();
            blocks.add(new Block(key, startLines[m], blockEnd, children));
        }
        return blocks;
    }

    private static int detectChildIndent(String[] lines, int start, int end, int parentIndent) {
        for (int i = start; i < end; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            int leading = leadingSpaces(line);
            return leading > parentIndent ? leading : -1;
        }
        return -1;
    }

    private static int leadingSpaces(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    private static List<String> sliceLines(String[] lines, int start, int end) {
        return new ArrayList<>(Arrays.asList(lines).subList(start, Math.min(end, lines.length)));
    }

    private static Map<String, Block> toMap(List<Block> blocks) {
        Map<String, Block> map = new LinkedHashMap<>();
        for (Block block : blocks) {
            map.putIfAbsent(block.key(), block);
        }
        return map;
    }

    /**
     * One key's line range within either the bundled or existing file: {@code startLine}
     * (inclusive) is the first claimed comment line or the key line itself if none,
     * {@code endLine} (exclusive) is the start of the next sibling block (or the end of the
     * parent's range for the last sibling). {@code children} is a best-effort structural guess
     * from indentation alone - callers only trust it once the corresponding
     * {@link ConfigurationSection} confirms this key really is a section on both sides.
     */
    private record Block(String key, int startLine, int endLine, List<Block> children) {
    }
}
