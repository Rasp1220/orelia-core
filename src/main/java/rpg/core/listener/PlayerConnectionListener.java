package rpg.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import rpg.core.player.PlayerDataManager;

/**
 * Bridges Bukkit connection events to {@link PlayerDataManager}. Contains no game logic;
 * it only triggers load/save of the generic player data container.
 */
public final class PlayerConnectionListener implements Listener {

    private final PlayerDataManager playerDataManager;

    public PlayerConnectionListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        playerDataManager.loadAsync(event.getUniqueId(), event.getName(), () -> {
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataManager.saveAndUnloadAsync(player.getUniqueId());
    }
}
