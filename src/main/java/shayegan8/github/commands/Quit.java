package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
			ChatPlugin.sendABMSG(sender, "chatp.quit.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 0) {
			ChatPlugin.sendACMSG(player, "chatp.quit.usage", getUsage());
			return;
		}
		final UUID senderUUID = player.getUniqueId();
		MDatabase.isPlayerInGroup(senderUUID).thenCompose(isInGroup -> {
			if (!isInGroup) {
				ChatPlugin.sendCMSG(player, "chatp.quit.notGroup", "&cYou are not in any group");
				return CompletableFuture.completedFuture(null);
			}
			return MDatabase.getPlayerTag(senderUUID);
		}).thenAccept(senderTag -> {
			if (senderTag == null)
				return;

			if (senderTag.equals("admin"))
				MDatabase.getPlayerGroup(senderUUID)
						.thenAccept(group -> MDatabase.getPlayersByGroup(group).thenAccept(stack -> stack
								.forEach(eachUUID -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
									MDatabase.setPlayerTag(eachUUID, "none");
									MDatabase.setPlayerGroup(eachUUID, "none");
									MDatabase.setPlayerInGroup(eachUUID, false);
									ChatPlugin.updateGui(group);
									ChatPlugin.sendACMSG(Bukkit.getPlayer(eachUUID), "chatp.quit.quit",
											"&eYou are no longer in this group");
								}))));
			else {
				MDatabase.setPlayerTag(senderUUID, "none");
				MDatabase.setPlayerGroup(senderUUID, "none");
				MDatabase.setPlayerInGroup(senderUUID, false);
				MDatabase.getPlayerGroup(senderUUID).thenAccept(group -> ChatPlugin.updateGui(group));
				ChatPlugin.sendCMSG(player, "chatp.quit.quit", "&eYou are no longer in this group");
			}
		});
	}
}
