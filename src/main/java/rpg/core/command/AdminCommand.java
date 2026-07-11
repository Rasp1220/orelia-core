package rpg.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.boss.BossModule;
import rpg.core.OreliaPlugin;
import rpg.monster.MonsterModule;
import rpg.monster.spawnpoint.model.MonsterSpawnPoint;
import rpg.monster.spawnpoint.service.MonsterSpawnPointService;

import java.util.UUID;

/**
 * {@code /rpgadmin <reload|spawn <monsterId>|spawnboss <bossId>|spawnpoint add|remove|list>}.
 * Individual module commands (item, job, quest, ...) live in their own module's
 * {@code command} package.
 *
 * <p>{@code spawn}/{@code spawnboss} are one-shot spawns for quick testing. Vanilla hostile
 * mob spawning is disabled by default (see {@code monster.disable-vanilla-hostile-spawning}
 * in config.yml), so real enemy encounters come from {@code spawnpoint}: stand where you
 * want monsters to appear, run {@code spawnpoint add}, and that location periodically
 * spawns up to its cap of that monster from then on (persisted across restarts).
 */
public final class AdminCommand implements CommandExecutor {

    private static final String USAGE = "Usage: /rpgadmin <reload|spawn <monsterId>|spawnboss <bossId>|spawnpoint <add|remove|list> ...>";
    private static final int DEFAULT_SPAWN_POINT_INTERVAL_SECONDS = 30;
    private static final int DEFAULT_SPAWN_POINT_MAX_ALIVE = 3;

    private final OreliaPlugin plugin;

    public AdminCommand(OreliaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + USAGE);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(ChatColor.GREEN + "Orelia configuration reloaded.");
            }
            case "spawn" -> spawnMonster(sender, args);
            case "spawnboss" -> spawnBoss(sender, args);
            case "spawnpoint" -> spawnPoint(sender, args);
            default -> sender.sendMessage(ChatColor.YELLOW + USAGE);
        }
        return true;
    }

    private void spawnMonster(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin spawn <monsterId>");
            return;
        }
        MonsterModule monsterModule = plugin.getModuleManager().get(MonsterModule.class).orElse(null);
        if (monsterModule == null) {
            sender.sendMessage(ChatColor.RED + "Monster module is not enabled.");
            return;
        }
        boolean spawned = monsterModule.getSpawnService().spawn(args[1], player.getLocation()).isPresent();
        sender.sendMessage(spawned ? ChatColor.GREEN + "Spawned " + args[1] + "."
                : ChatColor.RED + "Unknown monster id: " + args[1]);
    }

    private void spawnBoss(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin spawnboss <bossId>");
            return;
        }
        BossModule bossModule = plugin.getModuleManager().get(BossModule.class).orElse(null);
        if (bossModule == null) {
            sender.sendMessage(ChatColor.RED + "Boss module is not enabled.");
            return;
        }
        boolean spawned = bossModule.spawn(args[1], player.getLocation()).isPresent();
        sender.sendMessage(spawned ? ChatColor.GREEN + "Spawned boss " + args[1] + "."
                : ChatColor.RED + "Unknown boss id: " + args[1]);
    }

    private void spawnPoint(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin spawnpoint <add <monsterId> [intervalSeconds] [maxAlive]|remove <id>|list>");
            return;
        }
        MonsterModule monsterModule = plugin.getModuleManager().get(MonsterModule.class).orElse(null);
        if (monsterModule == null) {
            sender.sendMessage(ChatColor.RED + "Monster module is not enabled.");
            return;
        }
        MonsterSpawnPointService spawnPointService = monsterModule.getSpawnPointService();

        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin spawnpoint add <monsterId> [intervalSeconds] [maxAlive]");
                    return;
                }
                int intervalSeconds = parseIntOrDefault(args, 3, DEFAULT_SPAWN_POINT_INTERVAL_SECONDS);
                int maxAlive = parseIntOrDefault(args, 4, DEFAULT_SPAWN_POINT_MAX_ALIVE);
                var created = spawnPointService.add(player, args[2], intervalSeconds, maxAlive);
                if (created.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Unknown monster id: " + args[2]);
                    return;
                }
                sender.sendMessage(ChatColor.GREEN + "Registered spawn point " + created.get().getId() + " for " + args[2]
                        + " here (every " + intervalSeconds + "s, up to " + maxAlive + " alive).");
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin spawnpoint remove <id>");
                    return;
                }
                try {
                    boolean removed = spawnPointService.remove(UUID.fromString(args[2]));
                    sender.sendMessage(removed ? ChatColor.GREEN + "Removed spawn point " + args[2] + "."
                            : ChatColor.RED + "No spawn point with id " + args[2] + ".");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Not a valid spawn point id: " + args[2]);
                }
            }
            case "list" -> {
                var points = spawnPointService.getAll();
                if (points.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No spawn points registered.");
                    return;
                }
                sender.sendMessage(ChatColor.GREEN + "Spawn points:");
                for (MonsterSpawnPoint point : points.values()) {
                    sender.sendMessage(ChatColor.GRAY + "- " + point.getId() + " " + point.getMonsterId()
                            + " @ " + point.getWorld() + " " + (int) point.getX() + "," + (int) point.getY() + "," + (int) point.getZ()
                            + " (" + point.getIntervalSeconds() + "s, max " + point.getMaxAlive() + ")");
                }
            }
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgadmin spawnpoint <add <monsterId> [intervalSeconds] [maxAlive]|remove <id>|list>");
        }
    }

    private int parseIntOrDefault(String[] args, int index, int defaultValue) {
        if (args.length <= index) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
