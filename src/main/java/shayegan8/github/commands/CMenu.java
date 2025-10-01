package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;

public class CMenu extends CommandManager {

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatPlugin.sendBMSG(sender, "chatp.cmenu.notPlayer", "&cYou should be a player");
            return;
        }
        ChatPlugin.iMenu.onPlayerRequest(player);
        player.openInventory(ChatPlugin.iMenu.getInv());
    }
}
