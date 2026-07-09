package rpg.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import rpg.core.OreliaPlugin;

/**
 * {@code /rpgadmin reload} - re-reads every config.yml/*.yml and asks each module to
 * rebuild its in-memory state. Individual module commands (item, job, quest, ...) live
 * in their own module's {@code command} package.
 */
public final class AdminCommand implements CommandExecutor {

    private final OreliaPlugin plugin;

    public AdminCommand(OreliaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin reload");
            return true;
        }
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Orelia configuration reloaded.");
        return true;
    }
}
