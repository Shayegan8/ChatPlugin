package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Mute extends CommandManager {

	@Override
	public String getUsage() {
		return "&e/chatp mute playername";
	}

	@Override
	public String getPermission() {
		return "chatp.base.mute";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendBMSG(sender, "chatp.mute.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 1) {
			ChatPlugin.sendCMSG(player, "chatp.mute.usage", getUsage());
			return;
		}
		if (Bukkit.getPlayer(args[0]) == null) {

			ChatPlugin.sendCMSG(player, "chatp.mute.cantPlayer", "&cCant find this player");
			return;
		}
		final UUID senderUUID = player.getUniqueId();
		final UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

		MDatabase.isPlayerInGroup(senderUUID)
				.thenCombine(MDatabase.isPlayerInGroup(argUUID), (player1, player2) -> player1 && player2)
				.thenCompose(inGroup -> {
					if (!inGroup) {
						ChatPlugin.sendCMSG(player, "chatp.mute.notSame", "&cYou or that player should be in a group");
						return CompletableFuture.completedFuture(null);
					}
					return MDatabase.getPlayerTag(senderUUID).thenCombine(MDatabase.getPlayerTag(argUUID),
							(player1, player2) -> ChatPlugin.tags.get(player1) > ChatPlugin.tags.get(player2));
				}).thenAccept(obj -> {
					if (!obj)
						ChatPlugin.sendCMSG(player, "chatp.mute.cantMute", "&cYou cant mute this player");
					else {
						if (args[0].equals(sender.getName())) {
							ChatPlugin.sendCMSG(player, "chatp.mute.cantFuckYourself", "&cYou cant mute yourself");
							return;
						}
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
								() -> MDatabase.setPlayerMuted(argUUID, true));
						ChatPlugin.sendCMSG(player, "chatp.mute.muted", "&ePlayer muted");
					}
				}).exceptionallyAsync(exp -> {
					throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
				});
	}
}
