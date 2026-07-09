package rpg.gui.framework;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Lets {@link GuiListener} recover the {@link Gui} a click happened in.
 */
public final class GuiHolder implements InventoryHolder {

    private final Gui gui;

    public GuiHolder(Gui gui) {
        this.gui = gui;
    }

    public Gui getGui() {
        return gui;
    }

    @Override
    public Inventory getInventory() {
        return gui.toInventory();
    }
}
