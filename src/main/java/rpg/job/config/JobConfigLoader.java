package rpg.job.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.item.model.WeaponType;
import rpg.job.model.Job;
import rpg.job.model.JobType;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads {@code jobs.yml}, one section per {@link JobType}, into {@link Job} definitions.
 * A job with no matching section falls back to a definition with no weapon restriction
 * and no bonus, so a missing config entry never hard-fails plugin startup.
 */
public final class JobConfigLoader {

    public Map<JobType, Job> load(YamlConfiguration config) {
        Map<JobType, Job> jobs = new EnumMap<>(JobType.class);
        ConfigurationSection root = config.getConfigurationSection("jobs");
        for (JobType type : JobType.values()) {
            ConfigurationSection section = root == null ? null : root.getConfigurationSection(type.name());
            jobs.put(type, parse(type, section));
        }
        return jobs;
    }

    private Job parse(JobType type, ConfigurationSection section) {
        if (section == null) {
            return new Job(type, type.name(), Set.of(), StatSheet.empty());
        }
        String displayName = section.getString("display-name", type.name());

        Set<WeaponType> allowedWeapons = new HashSet<>();
        for (String raw : section.getStringList("allowed-weapons")) {
            try {
                allowedWeapons.add(WeaponType.valueOf(raw.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        StatSheet bonus = StatSheet.empty();
        ConfigurationSection bonusSection = section.getConfigurationSection("passive-bonus");
        if (bonusSection != null) {
            for (StatType statType : StatType.values()) {
                bonus.set(statType, bonusSection.getDouble(statType.name(), 0));
            }
        }

        return new Job(type, displayName, allowedWeapons, bonus);
    }
}
