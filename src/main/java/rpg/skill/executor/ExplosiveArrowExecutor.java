package rpg.skill.executor;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import rpg.skill.model.SkillData;

/**
 * Fires a single arrow tagged for {@link rpg.skill.listener.ExplosiveArrowHitListener} to
 * detonate on impact, dealing {@code radius}-sized AoE damage (爆裂矢).
 */
public final class ExplosiveArrowExecutor implements SkillExecutor {

    public static final String EXPLOSIVE_METADATA = "orelia_skill_explosive_arrow";

    private final Plugin plugin;

    public ExplosiveArrowExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player caster, SkillData data, int skillLevel) {
        double amount = data.scaledDamageMultiplier(skillLevel) * 4.0;
        Arrow arrow = caster.getWorld().spawnArrow(caster.getEyeLocation(), caster.getLocation().getDirection(), 2.5f, 0f);
        arrow.setShooter(caster);
        arrow.setMetadata(EXPLOSIVE_METADATA, new FixedMetadataValue(plugin, new double[] {amount, data.getRadius()}));
    }
}
