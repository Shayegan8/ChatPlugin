package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;

public class Quit extends CommandManager {

	@Override
	public String getUsage() {
		return "&e/chatp reload";
	}

	@Override
	public String getPermission() {
		return "chatp.base.quit";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendBMSG(sender, "chatp.quit.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 0) {
			ChatPlugin.sendCMSG(player, "chatp.quit.usage", getUsage());
			return;
		}
		UUID senderUUID = player.getUniqueId();
		MDatabase.isPlayerInGroup(senderUUID).thenAccept(isInGroup -> {
			if (!isInGroup)
				ChatPlugin.sendCMSG(player, "chatp.quit.notGroup", "&cYou are not in any group");
			else {
				Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
					MDatabase.setPlayerTag(senderUUID, "none");
					MDatabase.setPlayerGroup(senderUUID, "none");
					MDatabase.setPlayerInGroup(senderUUID, false);
				});
				ChatPlugin.sendCMSG(player, "chatp.quit.quit", "&eYou are no longer in this group");
			}
		}).exceptionallyAsync(exp -> {
			throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
		});
	}
}
