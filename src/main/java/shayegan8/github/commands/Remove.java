package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Remove extends CommandManager {

	@Override
	public String getUsage() {
		return "&e/chatp remove &cplayerName";
	}

	@Override
	public String getPermission() {
		return "chatp.base.remove";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendABMSG(sender, "chatp.remove.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 1) {
			ChatPlugin.sendACMSG(player, "chatp.remove.usage", getUsage());
			return;
		}
		if (Bukkit.getPlayer(args[0]) == null) {
			ChatPlugin.sendACMSG(player, "chatp.remove.notFound", "&cCant find this player :(");
			return;
		}
		final UUID senderUUID = player.getUniqueId();
		final UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

		MDatabase.getPlayerGroup(senderUUID).thenCombine(MDatabase.getPlayerGroup(argUUID), String::equals)
				.thenCompose(bothInSameGroup -> {
					if (!bothInSameGroup) {
						ChatPlugin.sendCMSG(player, "chatp.remove.notSame",
								"&cYour group and the requested player's group its not the same");
						return CompletableFuture.completedFuture(null);
					}
					return MDatabase.isPlayerInGroup(senderUUID).thenCombine(
							MDatabase.isPlayerInGroup(Bukkit.getPlayer(argUUID).getUniqueId()),
							(player1, player2) -> player1 && player2);
				}).thenCompose(bothAreInGroup -> {
					if (!bothAreInGroup) {
						ChatPlugin.sendCMSG(player, "chatp.remove.both", "&cboth players are not in group");
						return CompletableFuture.completedFuture(null);
					}
					return MDatabase.getPlayerTag(senderUUID);
				}).thenAccept(tag -> {
					if (tag.equals("admin") || tag.equals("staff")) {
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
							MDatabase.setPlayerGroup(argUUID, "none");
							MDatabase.setPlayerTag(argUUID, "none");
							MDatabase.setPlayerInGroup(argUUID, false);
							MDatabase.getPlayerGroup(senderUUID).thenAccept(group -> ChatPlugin.updateGui(group));
							ChatPlugin.sendACMSG(player, "chatp.remove.removed",
									"%player_name% &asuccessfully removed");
						});
					} else
						ChatPlugin.sendCMSG(player, "chatp.remove.cant", "&cYou are not admin or staff");
				}).exceptionally(exp -> {
					throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
				});
	}

}
