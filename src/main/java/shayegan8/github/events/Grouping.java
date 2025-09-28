package shayegan8.github.events;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;
import shayegan8.github.expansions.Placeholders;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class Grouping implements Listener {

    private final Plugin plugin;

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
                eachPlayer.sendMessage(m);
                ChatPlugin.getPlugin(ChatPlugin.class).getLogger().info(msg);
            }).exceptionally((exp) -> {
                exp.printStackTrace();
                return null;
            });
        });
    }

    @EventHandler
    public void onChat_(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = e.getPlayer().getUniqueId();
        MDatabase.isPlayerMuted(uuid).thenAccept(condition -> {
            if(!condition)
                return;
            Thread.ofVirtual().start(() -> {
                player.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("cantMSG", "You are muted")));
            });
            e.setCancelled(true);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!player.hasPlayedBefore()) {
            MDatabase.setPlayerGroup(uuid, "none");
            MDatabase.setPlayerTag(uuid, "none");
            MDatabase.setPlayerInGroup(uuid, false);
        }
        MDatabase.getPlayerGroup(uuid).thenAccept(group -> {
            Thread.ofVirtual().start(() -> {Placeholders.cache_group.put(uuid, group);});
        });
        MDatabase.getPlayerTag(uuid).thenAccept(tag -> {
            Thread.ofVirtual().start(() -> {Placeholders.cache_tag.put(uuid, tag);});
        });
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Thread.ofVirtual().start(() -> {Placeholders.cache_group.remove(uuid);});
        Thread.ofVirtual().start(() -> {Placeholders.cache_tag.remove(uuid);});
    }
}
