package rpg.gui.framework;

import org.bukkit.entity.Player;

/**
 * Opens a {@link Gui} for a player. Trivial today, but centralizing it means a future
 * "close current GUI before opening another" or transition-animation rule only needs to
 * change here.
 */
public final class GuiManager {

    public void open(Player player, Gui gui) {
        player.openInventory(gui.toInventory());
    }
}
