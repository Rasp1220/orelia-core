package rpg.item.service;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import rpg.item.model.ElementType;
import rpg.item.model.WeaponData;
import rpg.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the physical {@link ItemStack} for a {@link WeaponData} template: vanilla base
 * material chosen from the weapon type, display name/lore rendered from the template,
 * and the weapon id stamped into the PersistentDataContainer so {@link WeaponIdentityService}
 * can resolve it back later.
 */
public final class WeaponFactory {

    private final WeaponKeys keys;

    public WeaponFactory(WeaponKeys keys) {
        this.keys = keys;
    }

    public ItemStack create(WeaponData data) {
        Material baseMaterial = data.getWeaponType().materialForRarity(data.getRarity());

        List<String> lore = new ArrayList<>();
        lore.add(data.getRarity().getColor() + data.getRarity().name());
        lore.add("&7Lv. " + data.getWeaponLevel());
        lore.addAll(data.getDescription());
        lore.add("&c攻撃力 &f" + data.getAttackPower());
        if (data.getElement() != ElementType.NONE) {
            lore.add("&b属性 &f" + data.getElement());
        }
        lore.add("&e会心率 &f" + data.getCritRate() + "%");
        lore.add("&e会心倍率 &f" + data.getCritMultiplier() + "x");
        if (data.getRequiredJob() != null) {
            lore.add("&7必要職業 &f" + data.getRequiredJob());
        }
        lore.add("&7必要レベル &f" + data.getRequiredLevel());

        return new ItemBuilder(baseMaterial)
                .name(data.getRarity().getColor() + data.getName())
                .lore(lore)
                .customModelData(data.getCustomModelData())
                .tag(keys.weaponId(), PersistentDataType.STRING, data.getId())
                .build();
    }

}
