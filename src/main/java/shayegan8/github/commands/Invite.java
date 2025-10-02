package shayegan8.github.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Invite extends CommandManager {

	@Override
	public String getUsage() {
		return "&e/chatp invite playername";
	}

	@Override
	public String getPermission() {
		return "chatp.base.invite";
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendBMSG(sender, "chatp.invited.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 1) {
			ChatPlugin.sendCMSG(player, "chatp.invited.usage", getUsage());
			return;
		}
		if (sender.getName().equalsIgnoreCase(args[0])) {
			ChatPlugin.sendCMSG(player, "chatp.invited.cantFuckYourself", "&cYou cant invite yourself");
			return;
		}
		if (Bukkit.getPlayer(args[0]) == null) {
			ChatPlugin.sendCMSG(player, "chatp.invite.cantFind", "&cCant find this player");
			return;
		}
		final UUID senderUUID = player.getUniqueId();
		final UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
		MDatabase.getPlayerTag(senderUUID).thenCompose(tag -> {
			if (!tag.equalsIgnoreCase("admin") && !tag.equalsIgnoreCase("staff")) {
				ChatPlugin.sendCMSG(player, "chatp.invite.cant", "&cYou need to be admin or staff to invite");
				return CompletableFuture.completedFuture(null);
			}
			return MDatabase.getPlayerGroup(senderUUID);
		}).thenAccept(group -> {
			MDatabase.setPlayerInvited(argUUID, true);
			CompletableFuture.supplyAsync(
					() -> ChatPlugin.configuration.getString("chatp.invite.joinMSG",
							"%player_name% Invited you to %group_name%\n&eClick this messaage to join"),
					ChatPlugin.EVIRTUAL).thenAccept((joinMSG) -> {
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
							final TextComponent msg = new TextComponent(ColorUtils.C(player, joinMSG));
							msg.setColor(ChatColor.RED);
							msg.setClickEvent(new ClickEvent(ClickEvent.Action.CUSTOM, "/chatp join " + group));
							msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
									new ComponentBuilder(ColorUtils.C(player, ChatPlugin.configuration
											.getString("chatp.invite.bar", "&eClick this to join to the group")))
											.create()));
							sender.spigot().sendMessage(msg);
						});
					});
		}).exceptionallyAsync(exp -> {
			throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
		});
	}
}
