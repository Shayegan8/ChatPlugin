package shayegan8.github.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

public class Request extends CommandManager {

	@Override
	public String getPermission() {
		return "chatp.base.request";
	}

	@Override
	public String getUsage() {
		return "&e/chatp remove &cplayerName";
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			ChatPlugin.sendABMSG(sender, "chatp.request.notPlayer", "&cYou should be a player");
			return;
		}
		if (args.length != 1) {
			ChatPlugin.sendACMSG(player, "chatp.request.usage", getUsage());
			return;
		}
		if (Bukkit.getPlayer(args[0]) == null) {
			ChatPlugin.sendACMSG(player, "chatp.request.notFound", "&cCant find this player :(");
			return;
		}
		
		if(args[0].equals(sender.getName())) {
			ChatPlugin.sendACMSG(player, "chatp.request.cantFuckYourself", "&cYou can't send request to yourself");
			return;
		}

		final UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
		MDatabase.playerHasGroup(argUUID).thenAccept(hasGroup -> {
			if (!hasGroup) {
				ChatPlugin.sendCMSG(player, "chatp.request.dontGroup", "&cThat player dosent have group");
				return;
			}
			Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
				final String joinMSG = (String) ChatPlugin.entries.getOrDefault("chatp.request.joinMSG",
						"%player_name% requested to join to your group\n&eClick this messaage to accept request");
				final TextComponent msg = new TextComponent(ColorUtils.C(player, joinMSG));
				msg.setColor(ChatColor.RED);
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatp accept " + player.getName()));
				msg.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(ColorUtils.C(player, ChatPlugin.configuration
										.getString("chatp.request.bar", "&eClick this to join to the group")))
										.create()));
				MDatabase.getPlayerGroup(argUUID)
						.thenAccept(argGroup -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
								() -> MDatabase.setPlayerRequest(player.getUniqueId(), argGroup)));
				ChatPlugin.sendACMSG(player, "chatp.request.sent", "&aRequest successfully has been sent");
				Bukkit.getPlayer(argUUID).spigot().sendMessage(msg);

			});
		});

	}

}
