package shayegan8.github.events;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;
import shayegan8.github.expansions.Placeholders;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class Grouping implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		final UUID uuid = e.getPlayer().getUniqueId();
		if (Placeholders.cache_tag.get(uuid) == null)
			Placeholders.updateGroup(uuid);
		if (Placeholders.cache_tag.get(uuid) == null)
			Placeholders.updateTag(uuid);
		final String msg = e.getMessage();
		Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> e.setCancelled(true));
		Bukkit.getOnlinePlayers().stream().forEach(eachPlayer -> {
			MDatabase.getPlayerGroup(eachPlayer.getUniqueId())
					.thenCombine(MDatabase.getPlayerGroup(uuid), String::equalsIgnoreCase).thenCompose((condition) -> {
						if (condition) {
							final String formatedMSG = ColorUtils.C(e.getPlayer(), (String) ChatPlugin.entries
									.getOrDefault("chatp.playerchat", "&e%player_name% %chatp_group%&r&8: &7{msg}"));
							final String rp = formatedMSG.replace("{msg}", msg);
							return CompletableFuture.completedFuture(rp);
						}
						return null;
					}).thenAccept((m) -> {
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> eachPlayer.sendMessage(m));
						ChatPlugin.getInstance().getLogger().info(e.getPlayer().getName() + ": " + msg);
					});
		});
	}

	@SneakyThrows
	@EventHandler
	public void onChat_(AsyncPlayerChatEvent e) {
		final Player player = e.getPlayer();
		final UUID uuid = e.getPlayer().getUniqueId();
		MDatabase.isPlayerMuted(uuid).thenAccept(condition -> {
			if (condition) {
				ChatPlugin.sendCMSG(player, "chatp.cantMSG", "&cYou are muted in group");
				Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> e.setCancelled(true));
			}
		});
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		final UUID uuid = player.getUniqueId();
		MDatabase.playerHasGroup(uuid).thenAccept(has -> {
			if (has)
				return;
			Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
				MDatabase.setPlayerGroup(uuid, "none");
				MDatabase.setPlayerTag(uuid, "none");
				MDatabase.setPlayerInGroup(uuid, false);
			});
		});
		MDatabase.getPlayerGroup(uuid).thenAccept(group -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
				() -> Placeholders.cache_group.put(uuid, group)));
		MDatabase.getPlayerTag(uuid).thenAccept(tag -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
				() -> Placeholders.cache_tag.put(uuid, tag)));
	}

	@EventHandler
	public void onLeft(PlayerQuitEvent e) {
		final UUID uuid = e.getPlayer().getUniqueId();
		Placeholders.cache_group.remove(uuid);
		Placeholders.cache_tag.remove(uuid);
	}
}
