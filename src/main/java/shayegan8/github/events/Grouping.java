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
		e.setCancelled(true);
		final String msg = e.getMessage();
		Bukkit.getOnlinePlayers().stream().forEach(eachPlayer -> {
			MDatabase.getPlayerGroup(eachPlayer.getUniqueId())
					.thenCombine(MDatabase.getPlayerGroup(uuid), String::equals).thenCompose((condition) -> {
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
		Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
			MDatabase.playerHasGroup(uuid).thenAccept(has -> {
				System.out.println("player has not a group");
				if (!has)
					Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
						MDatabase.setPlayerGroup(uuid, "none");
						MDatabase.setPlayerTag(uuid, "none");
						MDatabase.setPlayerInGroup(uuid, false);
						System.out.println("player should be fucked now");
					});
			});

		});
	}

	@EventHandler
	public void onLeft(PlayerQuitEvent e) {
		final UUID uuid = e.getPlayer().getUniqueId();
		Placeholders.cache_group.computeIfPresent(uuid, (key, value) -> Placeholders.cache_group.remove(key));
		Placeholders.cache_tag.computeIfPresent(uuid, (key, value) -> Placeholders.cache_tag.remove(key));
	}
}
