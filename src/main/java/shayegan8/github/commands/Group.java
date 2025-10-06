package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;
import shayegan8.github.expansions.Placeholders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Group extends CommandManager {

	@Override
	public String getUsage() {
		return "&e/chatp group help,create,delete";
	}

	@Override
	public String getPermission() {
		return "chatp.base.group";
	}

	private void console(CommandSender sender, String[] args) {
		if (args.length == 2) {
			switch (args[0]) {
			case "create":
				MDatabase.createGroup(args[1]);
				ChatPlugin.sendABMSG(sender, "chatp.group.created", "&eGroup created");
				break;
			case "delete":
				MDatabase.deleteGroup(args[1]);
				ChatPlugin.sendABMSG(sender, "chatp.group.deleted", "&eGroup deleted");
				break;
			}
		} else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			CompletableFuture
					.supplyAsync(() -> ChatPlugin.configuration.getStringList("chatp.group.help"), ChatPlugin.EVIRTUAL)
					.thenAccept(ls -> ls.forEach(str -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
							() -> sender.sendMessage(ColorUtils.B(str)))))
					.exceptionallyAsync(exp -> {
						throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
					});
		} else
			ChatPlugin.sendABMSG(sender, "chatp.group.usage", getUsage());
	}

	@SuppressWarnings("unchecked")
	private void player(CommandSender sender, String[] args) {
		final Player player = (Player) sender;
		final UUID uuid = player.getUniqueId();
		if (args.length == 2) {
			switch (args[0]) {
			case "create":
				MDatabase.isPlayerInGroup(uuid).thenAccept((check) -> {
					if (check) {
						ChatPlugin.sendCMSG(player, "chatp.group.cantCreate", "&cYou are already in a group");
						return;
					}
					Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
						MDatabase.createGroup(args[1]);
						MDatabase.setPlayerInGroup(uuid, true);
						Placeholders.updateGroup(uuid);
						MDatabase.setPlayerTag(uuid, "admin");
						Placeholders.updateTag(uuid);
						MDatabase.setPlayerGroup(uuid, args[1]);
						ChatPlugin.updateGui(args[1]);
						ChatPlugin.sendACMSG(player, "chatp.group.created", "&e%chatp_group% &ahas been created");

					});
				});
				break;
			case "delete":
				MDatabase.isGroupExist(args[1]).thenAccept(groupExist -> {
					if (!groupExist) {
						ChatPlugin.sendCMSG(player, "chatp.group.notExist", "&cThat group dosent exist");
						return;
					}

					MDatabase.isPlayerInGroup(uuid).thenCompose((check) -> {
						if (!check) {
							ChatPlugin.sendCMSG(player, "chatp.group.notIn", "&cYou are not in any group");
							return CompletableFuture.completedFuture(null);
						}
						return MDatabase.getPlayerTag(uuid);
					}).thenAccept((tag) -> {
						if (!tag.equals("admin")) {
							ChatPlugin.sendCMSG(player, "chatp.group.admin", "&eYou are not admin");
							return;
						}
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
							MDatabase.getPlayersByGroup(args[1]).thenAccept(ls -> {
								ls.stream().forEach(eachUUID -> {
									final Player eachPlayer = Bukkit.getPlayer(eachUUID);
									if (!eachPlayer.isOnline())
										return;
									Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
										ChatPlugin.sendACMSG(Bukkit.getPlayer(uuid), "chatp.group.deleted",
												"&cYour group has been deleted");
										MDatabase.deleteGroup(args[1]);
										MDatabase.setPlayerInGroup(eachUUID, false);
										MDatabase.setPlayerTag(eachUUID, "none");
										Placeholders.updateTag(eachUUID);
										MDatabase.setPlayerGroup(eachUUID, "none");
										Placeholders.updateGroup(eachUUID);
										ChatPlugin.updateGui(args[1]);
									});
								});
							});
						});
					});
				});
				break;
			}
		} else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			CompletableFuture
					.supplyAsync(() -> (List<String>) ChatPlugin.entries.get("chatp.group.help"), ChatPlugin.EVIRTUAL)
					.thenAccept(ls -> {
						ls.forEach(str -> {
							Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
									() -> sender.sendMessage(ColorUtils.C(player, str)));
						});
					}).exceptionallyAsync(exp -> {
						throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
					});
		} else
			ChatPlugin.sendACMSG(player, "chatp.group.usage", getUsage());
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			console(sender, args);
		else
			player(sender, args);
	}
}
