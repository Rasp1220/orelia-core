package rpg.monster.service;

import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.util.Transformation;
import rpg.core.OreliaPlugin;
import rpg.util.ColorUtil;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Spawns a floating {@link TextDisplay} showing a damage number at {@code origin}, falling
 * under a (configurable, roughly half-vanilla-strength) gravity for a set number of ticks
 * before removing itself. Purely cosmetic combat feedback - works for any
 * {@link org.bukkit.entity.LivingEntity} taking damage, not just monsters.
 *
 * <p>Uses real entity physics ({@link org.bukkit.entity.Entity#setVelocity}) rather than
 * teleporting the display a fixed distance each tick: gravity is disabled
 * ({@link org.bukkit.entity.Entity#setGravity(boolean)}) so nothing else moves it, and a
 * downward velocity is accumulated by {@code gravity-per-tick} every tick and re-applied via
 * {@code setVelocity} - the display genuinely falls and accelerates, instead of drifting at a
 * constant speed.
 */
public final class DamageDisplayService {

    private final OreliaPlugin plugin;

    public DamageDisplayService(OreliaPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Location origin, double amount, boolean isCrit) {
        var config = plugin.getConfigManager().get("config.yml").get();
        if (!config.getBoolean("combat.damage-display.enabled", true)) {
            return;
        }
        long durationTicks = config.getLong("combat.damage-display.duration-ticks", 20);
        double gravityPerTick = config.getDouble("combat.damage-display.gravity-per-tick", 0.02);
        double yOffset = config.getDouble("combat.damage-display.y-offset", -0.3);
        String color = config.getString(isCrit ? "combat.damage-display.crit-color" : "combat.damage-display.normal-color",
                isCrit ? "&e" : "&f");
        float scale = isCrit ? (float) config.getDouble("combat.damage-display.crit-scale", 1.3) : 1.0f;

        Location spawnLocation = origin.clone().add(0, yOffset, 0);
        TextDisplay display = spawnLocation.getWorld().spawn(spawnLocation, TextDisplay.class, d -> {
            d.text(ColorUtil.component(color + Math.round(amount)));
            d.setBillboard(Display.Billboard.CENTER);
            d.setPersistent(false);
            d.setGravity(false);
            if (scale != 1.0f) {
                d.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(scale), new AxisAngle4f()));
            }
        });

        AtomicReference<BukkitTask> taskRef = new AtomicReference<>();
        long[] ticksElapsed = {0};
        double[] velocityY = {0.0};
        taskRef.set(plugin.getSchedulerService().runTimer(() -> {
            if (!display.isValid() || ticksElapsed[0] >= durationTicks) {
                display.remove();
                taskRef.get().cancel();
                return;
            }
            velocityY[0] -= gravityPerTick;
            display.setVelocity(new Vector(0, velocityY[0], 0));
            ticksElapsed[0]++;
        }, 1L, 1L));
    }
}
