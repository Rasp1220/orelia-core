package rpg.gui.screen;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
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
    private final MessageManager messages;

    public JobGuiScreen(JobService jobService, GuiConfig guiConfig, MessageManager messages) {
        this.jobService = jobService;
        this.guiConfig = guiConfig;
        this.messages = messages;
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
                if (changed) {
                    messages.send(clicker, "job.changed", "job", type.name());
                    clicker.closeInventory();
                } else {
                    messages.send(clicker, "job.change-failed");
                }
            }));
        }
        return gui;
    }
}
