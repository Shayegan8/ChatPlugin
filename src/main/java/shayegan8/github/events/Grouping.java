package shayegan8.github.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import shayegan8.github.database.MDatabase;

import java.util.UUID;

public class Grouping implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        String msg = e.getMessage();
        UUID uuid = e.getPlayer().getUniqueId();
        e.setCancelled(true);
        Bukkit.getOnlinePlayers().forEach(eachPlayer -> {
            MDatabase.getPlayerGroup(eachPlayer.getUniqueId()).thenCombine(MDatabase.getPlayerGroup(uuid), String::equalsIgnoreCase).thenAccept((condition) -> {
                if(condition)
                    eachPlayer.sendMessage(msg);
            });
        });
        System.out.println(msg);
        e.getPlayer().sendMessage(msg);
    }

    @EventHandler
    public void onChat_(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        MDatabase.isPlayerMuted(uuid).thenAccept(condition -> {
            if(!condition)
                return;
            e.setCancelled(true);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        MDatabase.isPlayerInGroup(uuid).thenAccept(condition -> {
           if(condition)
               return;
           MDatabase.setPlayerGroup(uuid, "none");
           MDatabase.setPlayerTag(uuid, "none");
           MDatabase.setPlayerInGroup(uuid, false);
        });
    }
}
