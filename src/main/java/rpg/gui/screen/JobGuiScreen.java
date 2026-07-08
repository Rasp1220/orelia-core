package rpg.gui.screen;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.gui.config.GuiConfig;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.job.model.JobType;
import rpg.job.service.JobService;
import rpg.util.ItemBuilder;

/**
 * Job-change screen (SOW section 17 "職業" / section 9 "職業変更はNPCから行う"). Opened by
 * the job-change NPC handler in the npc module.
 */
public final class JobGuiScreen {

    private final JobService jobService;
    private final GuiConfig guiConfig;

    public JobGuiScreen(JobService jobService, GuiConfig guiConfig) {
        this.jobService = jobService;
        this.guiConfig = guiConfig;
    }

    public Gui build(Player player) {
        Gui gui = new Gui(guiConfig.title("job", "&8職業変更"), 27);
        JobType current = jobService.getCurrentJob(player.getUniqueId()).orElse(null);

        int slot = 10;
        for (JobType type : JobType.values()) {
            boolean isCurrent = type == current;
            gui.set(slot++, new GuiButton(new ItemBuilder(isCurrent ? Material.GOLDEN_HELMET : Material.LEATHER_HELMET)
                    .name((isCurrent ? "&a" : "&f") + type)
                    .lore(isCurrent ? "&7現在の職業" : "&7クリックで転職")
                    .build(), (clicker, clickType) -> {
                if (isCurrent) {
                    return;
                }
                boolean changed = jobService.changeJob(clicker.getUniqueId(), type);
                clicker.sendMessage(changed ? ChatColor.GREEN + type + "に転職しました。" : ChatColor.RED + "転職に失敗しました。");
                if (changed) {
                    clicker.closeInventory();
                }
            }));
        }
        return gui;
    }
}
