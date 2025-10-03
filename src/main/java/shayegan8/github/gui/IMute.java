package shayegan8.github.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import shayegan8.github.ChatPlugin;

@Getter
public class IMute {

	private final Inventory inv;
	private final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();
	private final List<Integer> emptySlots;
	private final int size;
	
	public IMute() {
		emptySlots = ChatPlugin.configuration_menu.getIntegerList("gui.mute.empties.slots");
		size = ChatPlugin.configuration_menu.getInt("gui.mute.size", 54);
		inv = Bukkit.createInventory(null, size);
		ChatPlugin.configuration_menu.getConfigurationSection("gui.mute.list").getKeys(false).forEach(each -> {
			final String newStr = "gui.mute.list." + each;
			final ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(newStr);
			final String materialName = section.getString("material");
			final int amount = section.getInt("amount", 1);
			final List<Integer> slots = Collections.unmodifiableList(section.getIntegerList("slots"));
			final List<String> lore = Collections.unmodifiableList(section.getStringList("lore"));
			final String displayName = section.getString("displayName");
			ItemStack item;
			if (materialName.equals("PLAYER_HEAD"))
				item = ChatPlugin.getSkull(section.getString("texture"));
			else
				item = new ItemStack(Material.valueOf(materialName), amount);
			entries.put(newStr, new Entry(materialName, item, displayName, amount, slots, lore));
		});
	}
}