package shayegan8.github.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

public class Accept extends CommandManager {

	@Override
	public String getPermission() {
		return "chatp.base.accept";
	}

	@Override
	public String getUsage() {
		return "&e/chatp accept &cplayerName";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendABMSG(sender, "chatp.accept.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 1) {
			ChatPlugin.sendACMSG(player, "chatp.accept.usage", getUsage());
			return;
		}
		if (Bukkit.getPlayer(args[0]) == null) {
			ChatPlugin.sendACMSG(player, "chatp.accept.notFound", "&cCant find this player :(");
			return;
		}

		final UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
		MDatabase.getPlayerRequest(argUUID)
				.thenCombine(MDatabase.getPlayerGroup(player.getUniqueId()), String::equals)
				.thenAccept(groupSame -> {
					if (!groupSame) {
						ChatPlugin.sendCMSG(player, "chatp.accept.notSame",
								"&cThe player didn't request to join your group");
						return;
					}
					MDatabase.getPlayerGroup(player.getUniqueId()).thenAccept(senderGroup -> {
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
							final Player argPlayer = Bukkit.getPlayer(argUUID);
							MDatabase.setPlayerGroup(argUUID, senderGroup);
							MDatabase.setPlayerInGroup(argUUID, true);
							MDatabase.setPlayerRequest(argUUID, "none");
							ChatPlugin.sendACMSG(argPlayer, "chatp.accept.fucked",
									"&eYou are now in %chatp_group% group");
							ChatPlugin.updateMute(player);
						});
					});
				});
	}

}
