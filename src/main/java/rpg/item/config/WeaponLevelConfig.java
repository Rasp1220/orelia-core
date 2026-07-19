package rpg.item.config;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loads {@code config.yml: weapon-level.*} - how much each weapon level adds to base attack
 * power, and the player-level-gated cap on how far an individual weapon instance can be
 * leveled up (see {@link rpg.item.service.WeaponIdentityService#levelUp}).
 */
public final class WeaponLevelConfig {

    private double attackPowerFactor = 0.05;
    private int initialCap = 5;
    private int tierStep = 10;

    public void load(YamlConfiguration config) {
        attackPowerFactor = config.getDouble("weapon-level.attack-power-factor", 0.05);
        initialCap = config.getInt("weapon-level.initial-cap", 5);
        tierStep = config.getInt("weapon-level.tier-step", 10);
    }

    /** Attack power multiplier bonus per weapon level, e.g. 0.05 = +5% base attack power per level. */
    public double getAttackPowerFactor() {
        return attackPowerFactor;
    }

    /**
     * Highest weapon level a player may level a weapon up to, given their character level.
     * Below {@code tierStep} player levels, the cap is {@code initialCap}; from
     * {@code tierStep} onward it rises in {@code tierStep}-sized steps (e.g. with the
     * defaults: player level 1-9 -&gt; cap 5, 10-19 -&gt; 10, 20-29 -&gt; 20, 30-39 -&gt; 30, ...).
     */
    public int weaponLevelCap(int playerLevel) {
        if (playerLevel < tierStep) {
            return initialCap;
        }
        return (playerLevel / tierStep) * tierStep;
    }
}
