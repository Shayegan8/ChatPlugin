package shayegan8.github;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ColorUtils {

    public static String C(Player player, String message) {
        return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message));
    }

    public static String B(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
