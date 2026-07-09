package rpg.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * One clickable (or purely decorative) slot in a {@link Gui}.
 */
public final class GuiButton {

    /** Invoked on click; receives the player and the click type name (e.g. "LEFT", "RIGHT"). */
    public interface ClickAction {
        void onClick(Player player, String clickType);
    }

    private final ItemStack icon;
    private final ClickAction action;

    public GuiButton(ItemStack icon, ClickAction action) {
        this.icon = icon;
        this.action = action;
    }

    public static GuiButton display(ItemStack icon) {
        return new GuiButton(icon, (player, clickType) -> {
        });
    }

    public ItemStack getIcon() {
        return icon;
    }

    public ClickAction getAction() {
        return action;
    }
}
