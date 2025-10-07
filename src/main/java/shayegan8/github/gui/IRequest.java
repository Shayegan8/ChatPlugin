package shayegan8.github.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import shayegan8.github.ChatPlugin;

public final class IRequest {

	@Getter
	private final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();
	@Getter
	private final List<Integer> empties;
	@Getter
	private final String title;
	@Getter
	private final List<String> lore;
	@Getter
	private final int size;
	
	public final static String NAME = "request";

	public IRequest() {
		lore = ChatPlugin.configuration_menu.getStringList("gui.request.emptyLore").stream().collect(Collectors.toUnmodifiableList());
		title = ChatPlugin.configuration_menu.getString("gui.request.title", "&cRequest to groups");
		empties = ChatPlugin.configuration_menu.getIntegerList("gui.request.empties.slots").stream().collect(Collectors.toUnmodifiableList());
		size = ChatPlugin.configuration_menu.getInt("gui.request.size", 54);
		ChatPlugin.configuration_menu.getConfigurationSection("gui.request.list").getKeys(false).forEach(each -> {
			String newStr = "gui.request.list." + each;
			System.out.println(newStr);
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
