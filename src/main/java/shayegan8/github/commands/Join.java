package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;

public class Join extends CommandManager {

	@Override
	public String getUsage() {
		return "&e/chatp join groupname";
	}

	@Override
	public String getPermission() {
		return "chatp.base.join";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendABMSG(sender, "chatp.join.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 1) {
			ChatPlugin.sendACMSG(player, "chatp.join.usage", getUsage());
			return;
		}
		final UUID senderUUID = player.getUniqueId();
		MDatabase.isPlayerInvited(senderUUID).thenAccept(invited -> {
			if (!invited)
				ChatPlugin.sendCMSG(player, "chatp.mute.invitedFirst", "&eYou should be invited first");
			else {
				MDatabase.setPlayerGroup(senderUUID, args[0]);
				MDatabase.setPlayerInGroup(senderUUID, true);
				ChatPlugin.sendCMSG(player, "chatp.join.invited", "&eYou are now in %chatp_group% group");
			}
		}).exceptionallyAsync(exp -> {
			throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
		});
	}
}
