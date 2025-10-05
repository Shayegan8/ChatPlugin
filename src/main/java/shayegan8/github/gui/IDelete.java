package shayegan8.github.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import shayegan8.github.ChatPlugin;

@Getter
public final class IDelete extends Gui {

	private final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();
	private final String title;
	private final int size;

	public IDelete() {
		title = ChatPlugin.configuration_menu.getString("gui.delete.title", "&cAre you sure about it?");
		size = ChatPlugin.configuration_menu.getInt("gui.delete.size", 9);
		ChatPlugin.configuration_menu.getConfigurationSection("gui.delete.list").getKeys(false).forEach(each -> {
			String newStr = "gui.delete.list." + each;
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

}
