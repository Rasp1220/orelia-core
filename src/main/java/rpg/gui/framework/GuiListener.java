package rpg.gui.framework;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

/**
 * The single click handler for every {@link Gui} screen in the plugin. Cancels all
 * interaction with GUI inventories (they are display/action-button screens, not storage)
 * and dispatches to the clicked slot's {@link GuiButton}.
 *
 * <p>The button's action is run one tick later rather than inline. Several buttons (e.g.
 * every player-info nether-star category/back button, {@code GuiApi#openSkill}) call
 * {@link GuiManager#open} to switch to a different {@link Gui} from within this same click
 * event. Doing that synchronously races the client's own click-transaction resolution for
 * the slot being clicked - despite the event already being cancelled here, the client was
 * observed to render the icon as picked up onto the cursor instead of showing the new
 * screen. Deferring the action lets this click finish resolving first.
 */
public final class GuiListener implements Listener {

    private final Plugin plugin;

    public GuiListener(Plugin plugin) {
        this.plugin = plugin;
    }

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
        if (button == null) {
            return;
        }
        String clickType = event.getClick().name();
        Bukkit.getScheduler().runTask(plugin, () -> button.getAction().onClick(player, clickType));
    }
}
