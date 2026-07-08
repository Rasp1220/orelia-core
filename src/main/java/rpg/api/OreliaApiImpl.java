package rpg.api;

import org.bukkit.inventory.ItemStack;
import rpg.core.OreliaPlugin;
import rpg.item.ItemModule;
import rpg.job.JobModule;
import rpg.status.StatusModule;
import rpg.status.model.PlayerStatusComponent;
import rpg.status.model.StatSheet;
import rpg.status.model.StatType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Adapts the internal module services to {@link OreliaApi}. Package-private-by-convention -
 * external plugins should only ever hold a reference to the {@link OreliaApi} interface,
 * obtained via the Bukkit ServicesManager, not this class.
 */
final class OreliaApiImpl implements OreliaApi {

    private final OreliaPlugin plugin;

    OreliaApiImpl(OreliaPlugin plugin) {
        this.plugin = plugin;
    }

    private StatusModule status() {
        return plugin.getModuleManager().get(StatusModule.class).orElseThrow();
    }

    private JobModule job() {
        return plugin.getModuleManager().get(JobModule.class).orElseThrow();
    }

    private ItemModule item() {
        return plugin.getModuleManager().get(ItemModule.class).orElseThrow();
    }

    @Override
    public Optional<Integer> getPlayerLevel(UUID playerId) {
        return status().getStatusService().component(playerId).map(PlayerStatusComponent::getLevel);
    }

    @Override
    public Optional<String> getPlayerJob(UUID playerId) {
        return job().getJobService().getCurrentJob(playerId).map(Enum::name);
    }

    @Override
    public Map<String, Double> getPlayerStats(UUID playerId) {
        StatSheet sheet = status().getStatusService().getFinalStats(playerId).orElse(StatSheet.empty());
        Map<String, Double> result = new HashMap<>();
        for (StatType type : StatType.values()) {
            result.put(type.name(), sheet.get(type));
        }
        return result;
    }

    @Override
    public Optional<String> getHeldWeaponId(UUID playerId) {
        var player = plugin.getServer().getPlayer(playerId);
        if (player == null) {
            return Optional.empty();
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        return item().getItemManager().getIdentityService().idOf(weapon);
    }

    @Override
    public Set<String> getAllWeaponIds() {
        return item().getItemManager().getAllWeapons().keySet();
    }
}
