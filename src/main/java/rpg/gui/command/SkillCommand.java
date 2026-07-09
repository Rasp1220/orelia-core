package rpg.gui.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.gui.framework.GuiManager;
import rpg.gui.screen.SkillGuiScreen;

/**
 * {@code /rpgskill} - opens the sender's weapon-skill GUI (learn/upgrade/socket skills for
 * the weapon currently held).
 */
public final class SkillCommand implements CommandExecutor {

    private final GuiManager guiManager;
    private final SkillGuiScreen skillGuiScreen;

    public SkillCommand(GuiManager guiManager, SkillGuiScreen skillGuiScreen) {
        this.guiManager = guiManager;
        this.skillGuiScreen = skillGuiScreen;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        guiManager.open(player, skillGuiScreen.build(player));
        return true;
    }
}
