package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import shayegan8.github.ChatPlugin;
import shayegan8.github.gui.IMenu;

public class ReloadC extends CommandManager {

	@Override
	public String getPermission() {
		return "chatp.base.reload";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ChatPlugin.loadChat();
		ChatPlugin.iMenu = new IMenu();
		sender.sendMessage("Plugin configuration reloaded");
	}
}
