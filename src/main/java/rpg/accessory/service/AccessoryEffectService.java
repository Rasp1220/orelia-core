package rpg.accessory.service;

import org.bukkit.entity.Player;
import rpg.accessory.manager.AccessorySlotLayout;
import rpg.accessory.model.AccessoryData;
import rpg.accessory.model.AccessoryType;
import rpg.status.service.StatusService;

/**
 * Applies/removes the stat bonus of an equipped accessory to the status module, and
 * resyncs every slot (e.g. on player join, when the runtime contribution map is empty
 * again after being rebuilt from scratch).
 */
public final class AccessoryEffectService {

    private final StatusService statusService;
    private final AccessoryIdentityService identityService;

    public AccessoryEffectService(StatusService statusService, AccessoryIdentityService identityService) {
        this.statusService = statusService;
        this.identityService = identityService;
    }

    private static String sourceKey(AccessoryType type) {
        return "accessory:" + type.name();
    }

    public void applyFromSlot(Player player, AccessoryType type) {
        int slot = AccessorySlotLayout.slotFor(type);
        AccessoryData data = identityService.dataOf(player.getInventory().getStorageContents()[slot]).orElse(null);
        if (data == null || data.getType() != type) {
            clear(player, type);
            return;
        }
        statusService.setEquipmentContribution(player.getUniqueId(), sourceKey(type), data.getStatBonus());
    }

    public void clear(Player player, AccessoryType type) {
        statusService.clearEquipmentContribution(player.getUniqueId(), sourceKey(type));
    }

    public void syncAll(Player player) {
        for (AccessoryType type : AccessoryType.values()) {
            applyFromSlot(player, type);
        }
    }
}
