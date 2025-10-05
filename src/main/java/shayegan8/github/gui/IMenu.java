package shayegan8.github.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import shayegan8.github.ChatPlugin;

@Getter
public final class IMenu extends Gui {

	private final Inventory inv;
	private final int size;
	private final String title;
	private final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();

	public IMenu() {
		title = ChatPlugin.configuration_menu.getString("gui.menu.title", "&cChatPlugin menu");
		size = ChatPlugin.configuration_menu.getInt("gui.menu.size", 54);
		inv = Bukkit.createInventory(null, size);
		ChatPlugin.configuration_menu.getConfigurationSection("gui.menu.list").getKeys(false).forEach(each -> {
			String newStr = "gui.menu.list." + each;
			ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(newStr);
			String materialName = section.getString("material");
			int amount = section.getInt("amount", 1);
			List<Integer> slots = Collections.unmodifiableList(section.getIntegerList("slots"));
			List<String> lore = Collections.unmodifiableList(section.getStringList("lore"));
			String displayName = section.getString("displayName");
			ItemStack item;
			if (materialName.equals("PLAYER_HEAD"))
				item = ChatPlugin.getSkull(section.getString("texture"));
			else
				item = new ItemStack(Material.valueOf(materialName), amount);
			entries.put(newStr, new Entry(materialName, item, displayName, amount, slots, lore));
		});
	}
	

	@Override
	public String getName() {
		return "menu";
	}
}
