package rpg.gui.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@code gui.yml: titles.<screen>} so server owners can reword/translate screen
 * titles without touching code. Falls back to the Japanese default baked into each
 * screen class when a key is absent.
 */
public final class GuiConfig {

    private final Map<String, String> titles = new HashMap<>();

    public void load(YamlConfiguration config) {
        titles.clear();
        var section = config.getConfigurationSection("titles");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                titles.put(key, section.getString(key));
            }
        }
    }

    public String title(String key, String defaultTitle) {
        return titles.getOrDefault(key, defaultTitle);
    }
}
