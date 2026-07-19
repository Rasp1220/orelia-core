package rpg.item.service;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import rpg.item.config.WeaponLevelConfig;
import rpg.item.model.WeaponData;
import rpg.item.repository.WeaponRepository;

import java.util.Optional;

/**
 * Resolves an in-game {@link ItemStack} back to the {@link WeaponData} template it was
 * generated from, by reading the id stamped by {@link WeaponFactory}.
 */
public final class WeaponIdentityService {

    private final WeaponKeys keys;
    private final WeaponRepository repository;
    private final WeaponLevelConfig levelConfig;

    public WeaponIdentityService(WeaponKeys keys, WeaponRepository repository, WeaponLevelConfig levelConfig) {
        this.keys = keys;
        this.repository = repository;
        this.levelConfig = levelConfig;
    }

    public Optional<String> idOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        ItemMeta meta = stack.getItemMeta();
        String id = meta.getPersistentDataContainer().get(keys.weaponId(), PersistentDataType.STRING);
        return Optional.ofNullable(id);
    }

    public Optional<WeaponData> dataOf(ItemStack stack) {
        return idOf(stack).flatMap(repository::findById);
    }

    /** Enhancement level applied by the "強化屋" NPC (SOW section 12), 0 for a freshly created weapon. */
    public int getEnhancementLevel(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return 0;
        }
        Integer level = stack.getItemMeta().getPersistentDataContainer().get(keys.enhancementLevel(), PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    /** Increments the weapon's enhancement level by one and returns the new level. */
    public int enhance(ItemStack stack) {
        int newLevel = getEnhancementLevel(stack) + 1;
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keys.enhancementLevel(), PersistentDataType.INTEGER, newLevel);
        stack.setItemMeta(meta);
        return newLevel;
    }

    /** Attack-power scale factor from enhancement level: +10% per level. */
    public double enhancementMultiplier(ItemStack stack) {
        return 1.0 + getEnhancementLevel(stack) * 0.1;
    }

    /**
     * This weapon instance's current level - starts at the weapon type's {@code items.yml}
     * {@code level:} ({@link WeaponData#getWeaponLevel()}) the first time it's read, since
     * {@link WeaponFactory} doesn't stamp it at creation. Distinct from
     * {@link #getEnhancementLevel}.
     */
    public int getWeaponLevel(ItemStack stack, WeaponData data) {
        if (stack == null || !stack.hasItemMeta()) {
            return data.getWeaponLevel();
        }
        Integer level = stack.getItemMeta().getPersistentDataContainer().get(keys.weaponLevel(), PersistentDataType.INTEGER);
        return level == null ? data.getWeaponLevel() : level;
    }

    /**
     * Highest weapon level {@code playerLevel} is currently allowed to level this weapon up
     * to - see {@link WeaponLevelConfig#weaponLevelCap}.
     */
    public int weaponLevelCap(int playerLevel) {
        return levelConfig.weaponLevelCap(playerLevel);
    }

    /**
     * Attempts to raise this weapon instance's level by one. Returns the new level, or
     * {@code -1} if {@code playerLevel} isn't high enough to unlock the next level (see
     * {@link #weaponLevelCap}).
     */
    public int levelUp(ItemStack stack, WeaponData data, int playerLevel) {
        int current = getWeaponLevel(stack, data);
        int next = current + 1;
        if (next > weaponLevelCap(playerLevel)) {
            return -1;
        }
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keys.weaponLevel(), PersistentDataType.INTEGER, next);
        stack.setItemMeta(meta);
        return next;
    }

    /** {@code attack-power * (1 + weaponLevel * weaponLevelFactor) * enhancementMultiplier} - the weapon's full base attack power. */
    public double baseAttackPower(ItemStack stack, WeaponData data) {
        double weaponLevelBonus = 1.0 + getWeaponLevel(stack, data) * levelConfig.getAttackPowerFactor();
        return data.getAttackPower() * weaponLevelBonus * enhancementMultiplier(stack);
    }
}
