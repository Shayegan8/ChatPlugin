package shayegan8.github.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

public class PPlayer extends PlaceholderExpansion {

    private final Plugin plugin;

    public PPlayer(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatp-group";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join("", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String parms) {
        var group = ChatPlugin.configuration.get(this.getIdentifier());
        var group_menu = ChatPlugin.configuration.get(this.getIdentifier());

        return null;
    }

}
