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

@Getter
public final class IInvite {
	
	private final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();
	private final List<Integer> empties;
	private final String title;
	private final List<String> lore;
	private final int size;
	
	public IInvite() {
		lore = ChatPlugin.configuration_menu.getStringList("gui.invite.emptyLore").stream()
				.collect(Collectors.toUnmodifiableList());
		title = ChatPlugin.configuration_menu.getString("gui.invite.title", "&cInvite players to your group");
		empties = ChatPlugin.configuration_menu.getIntegerList("gui.invite.empties.slots");
		size = ChatPlugin.configuration_menu.getInt("gui.invite.size", 54);
		ChatPlugin.configuration_menu.getConfigurationSection("gui.invite.list").getKeys(false).forEach(each -> {
			String newStr = "gui.invite.list." + each;
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
