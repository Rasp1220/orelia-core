package rpg.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * The single click handler for every {@link Gui} screen in the plugin. Cancels all
 * interaction with GUI inventories (they are display/action-button screens, not storage)
 * and dispatches to the clicked slot's {@link GuiButton}.
 */
public final class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder)) {
            return;
        }
        Gui gui = holder.getGui();
        if (!gui.isItemMovementAllowed()) {
            event.setCancelled(true);
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        GuiButton button = gui.getButton(event.getSlot());
        if (button != null) {
            button.getAction().onClick(player, event.getClick().name());
        }
    }
}
