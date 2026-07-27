package shayegan8.github.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import org.bukkit.Bukkit;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

@Getter
public final class IMute {

    private final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();
    private final List<Integer> empties;
    private final String title;
    private final List<String> lore;
    private final int size;

    public IMute() {
        lore = ChatPlugin.configuration_menu.getStringList("gui.mute.emptyLore").stream()
                .collect(Collectors.toUnmodifiableList());
        title = ColorUtils.B(ChatPlugin.configuration_menu.getString("gui.mute.title", "&cChatPlugin mute menu"));
        empties = ChatPlugin.configuration_menu.getIntegerList("gui.mute.empties.slots").stream()
                .collect(Collectors.toUnmodifiableList());
        size = ChatPlugin.configuration_menu.getInt("gui.mute.size", 54);
        CompletableFuture.runAsync(() -> {
            ChatPlugin.configuration_menu.getConfigurationSection("gui.mute.list").getKeys(false).forEach(each -> {
                Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
                    String newStr = "gui.mute.list." + each;
                    ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(newStr);
                    String materialName = section.getString("material");
                    int amount = section.getInt("amount", 1);
                    List<Integer> slots = Collections.unmodifiableList(section.getIntegerList("slots"));
                    List<String> lore = Collections.unmodifiableList(section.getStringList("lore"));
                    String displayName = section.getString("displayName");
                    ItemStack item;
                    if (materialName.equals("PLAYER_HEAD")) {
                        item = ChatPlugin.getSkull(section.getString("texture"));
                    } else {
                        item = new ItemStack(Material.valueOf(materialName), amount);
                    }
                    entries.put(newStr, new Entry(materialName, item, displayName, amount, slots, lore));
                });
            });
        }, ChatPlugin.EVIRTUAL);

    }
}
