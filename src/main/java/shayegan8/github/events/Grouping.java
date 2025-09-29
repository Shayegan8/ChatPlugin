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

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class Grouping implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        String msg = e.getMessage();
        UUID uuid = e.getPlayer().getUniqueId();
        e.setCancelled(true);
        Bukkit.getOnlinePlayers().parallelStream().forEach(eachPlayer -> {
            MDatabase.getPlayerGroup(eachPlayer.getUniqueId()).thenCombine(MDatabase.getPlayerGroup(uuid), String::equalsIgnoreCase).thenComposeAsync((condition) -> {
                if(condition) {
                    String formatedMSG = ColorUtils.C(e.getPlayer(), ChatPlugin.configuration.getString("playerChat", "&e%player_name% %chatp_group%&r&8: &7{msg}"));
                    String rp = formatedMSG.replace("{msg}", msg);
                    return CompletableFuture.completedFuture(rp);
                }
                return null;
            }).thenAccept((m) -> {
                Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> eachPlayer.sendMessage(m));
                ChatPlugin.getInstance().getLogger().info(msg);
            }).exceptionally((exp) -> {
                throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
            });
        });
    }

    @SneakyThrows
    @EventHandler
    public void onChat_(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = e.getPlayer().getUniqueId();
        MDatabase.isPlayerMuted(uuid).thenComposeAsync(condition -> {
            if(condition)
                return CompletableFuture.completedFuture(ColorUtils.C(player, ChatPlugin.configuration.getString("cantMSG", "You are muted")));
            return CompletableFuture.completedFuture(null);
        }).thenAccept(result -> {
            if(result != null)
                Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> player.sendMessage(result));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        MDatabase.playerHasGroup(uuid).thenAccept(has -> {
           if(!has)
               return;
           MDatabase.setPlayerGroup(uuid, "none");
           MDatabase.setPlayerTag(uuid, "none");
           MDatabase.setPlayerInGroup(uuid, false);
        });
        MDatabase.getPlayerGroup(uuid).thenAccept(group -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> Placeholders.cache_group.put(uuid, group)));
        MDatabase.getPlayerTag(uuid).thenAccept(tag -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),() -> Placeholders.cache_tag.put(uuid, tag)));
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> Placeholders.cache_group.remove(uuid));
        Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> Placeholders.cache_tag.remove(uuid));
    }
}
