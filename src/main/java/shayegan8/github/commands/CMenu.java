package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import shayegan8.github.ChatPlugin;

public class CMenu extends CommandManager {

	@Override
	public String getPermission() {
		return "chatp.base.menu";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendBMSG(sender, "chatp.cmenu.notPlayer", "&cYou should be a player");
			return;
		}
		ChatPlugin.onPlayerRequest(player, ChatPlugin.iMenu.getEntries(), ChatPlugin.iMenu.getInv());
		player.openInventory(ChatPlugin.iMenu.getInv());
	}
}
