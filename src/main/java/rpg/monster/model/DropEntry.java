package rpg.monster.model;

/**
 * One drop table row. {@code weaponId} references an {@link rpg.item.model.WeaponData}
 * id when {@code vanillaMaterial} is null, otherwise a plain vanilla {@link org.bukkit.Material}
 * name is used - this keeps monster loot tables usable before every weapon id exists yet.
 */
public final class DropEntry {

    private final String weaponId;
    private final String vanillaMaterial;
    private final double chancePercent;
    private final int minAmount;
    private final int maxAmount;

    public DropEntry(String weaponId, String vanillaMaterial, double chancePercent, int minAmount, int maxAmount) {
        this.weaponId = weaponId;
        this.vanillaMaterial = vanillaMaterial;
        this.chancePercent = chancePercent;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public String getWeaponId() {
        return weaponId;
    }

    public String getVanillaMaterial() {
        return vanillaMaterial;
    }

    public double getChancePercent() {
        return chancePercent;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }
}
