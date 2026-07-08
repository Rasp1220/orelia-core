package rpg.gui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import rpg.gui.framework.GuiHolder;
import rpg.gui.repository.WarehouseRepository;
import rpg.gui.screen.WarehouseGuiScreen;

/**
 * Persists warehouse contents back to storage the moment a player closes it.
 */
public final class WarehouseSaveListener implements Listener {

    private final WarehouseRepository repository;

    public WarehouseSaveListener(WarehouseRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder)) {
            return;
        }
        if (!WarehouseGuiScreen.TAG.equals(holder.getGui().getTag())) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        repository.save(player.getUniqueId(), event.getInventory().getContents());
    }
}
