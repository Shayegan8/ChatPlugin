package shayegan8.github.expansions;

import lombok.SneakyThrows;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.database.MDatabase;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PPlayer extends PlaceholderExpansion {

    private final Plugin plugin;
    public Map<UUID, ObjectWithTime> cache_group = new ConcurrentHashMap<>();
    public Map<UUID, ObjectWithTime> cache_tag = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PPlayer(Plugin plugin) {
        this.plugin = plugin;
        scheduler.scheduleAtFixedRate(this::clean, 60, 60, TimeUnit.SECONDS);
    }

    private void clean() {
        for (UUID key : cache_group.keySet()) {
            ObjectWithTime objT = cache_group.get(key);
            if(objT != null && System.currentTimeMillis() - objT.time() >= 60000)
                cache_group.remove(key);
        }
        for (UUID key : cache_tag.keySet()) {
            ObjectWithTime objT = cache_tag.get(key);
            if(objT != null && System.currentTimeMillis() - objT.time() >= 60000)
                cache_tag.remove(key);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatp";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().getFirst();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @SneakyThrows
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String parms) {
        if(parms.equalsIgnoreCase("group")) {
            updateGroup(player.getUniqueId());
            return getGroup(player.getUniqueId());
        } else if(parms.equalsIgnoreCase("tag")) {
            updateTag(player.getUniqueId());
            return getTag(player.getUniqueId());
        }
        return null;
    }

    private String getGroup(UUID uuid) {
        ObjectWithTime objT = cache_group.get(uuid);
        if(objT == null)
            return null;
        return objT.result();
    }

    private String getTag(UUID uuid) {
        ObjectWithTime objT = cache_tag.get(uuid);
        if(objT == null)
            return null;
        return objT.result();
    }


    private void updateGroup(UUID uuid) {
        MDatabase.getPlayerGroup(uuid).thenAccept(group -> {
            Thread.ofVirtual().start(() -> cache_group.put(uuid, new ObjectWithTime(group, System.currentTimeMillis())));
        });
    }

    private void updateTag(UUID uuid) {
        MDatabase.getPlayerTag(uuid).thenAccept(tag -> {
            Thread.ofVirtual().start(() -> cache_tag.put(uuid, new ObjectWithTime(tag, System.currentTimeMillis())));
        });
    }

}
