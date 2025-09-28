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

public class Placeholders extends PlaceholderExpansion {

    private final Plugin plugin;
    public static Map<UUID, String> cache_group = new ConcurrentHashMap<>();
    public static Map<UUID, String> cache_tag = new ConcurrentHashMap<>();

    public Placeholders(Plugin plugin) {
        this.plugin = plugin;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(this::cleaner, 60, 60, TimeUnit.SECONDS);
    }

    private void cleaner() {
        cache_group.clear();
        cache_tag.clear();
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
        UUID uuid = player.getUniqueId();
        if(parms.equalsIgnoreCase("group")) {
            if(cache_group.get(uuid) == null)
                updateGroup(uuid);
            return getGroup(uuid);
        } else if(parms.equalsIgnoreCase("tag")) {
            if(cache_tag.get(uuid) == null)
                updateTag(uuid);
            return getTag(uuid);
        }
        return null;
    }

    private String getGroup(UUID uuid) {
        return cache_group.getOrDefault(uuid, "loading");
    }

    private String getTag(UUID uuid) {
        return cache_tag.getOrDefault(uuid, "loading");
    }

    public static void updateGroup(UUID uuid) {
        MDatabase.getPlayerGroup(uuid).thenAccept(group -> {
            cache_tag.remove(uuid);
            cache_group.put(uuid, group);
        });
    }

    public static void updateTag(UUID uuid) {
        MDatabase.getPlayerTag(uuid).thenAccept(tag -> {
            cache_tag.remove(uuid);
            cache_tag.put(uuid, tag);
        });
    }
}
