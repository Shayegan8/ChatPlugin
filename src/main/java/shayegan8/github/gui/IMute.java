package shayegan8.github.gui;

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

	private Inventory inv;
	private Map<String, ItemSave> items = new ConcurrentHashMap<String, ItemSave>();
	private Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();

	public IMute() {
	    inv = Bukkit.createInventory(null, ChatPlugin.configuration_menu.getInt("gui.mute.size", 54));
		ChatPlugin.configuration_menu.getConfigurationSection("gui.mute.list").getKeys(false).forEach(each -> {
			String newStr = "gui.mute.list." + each;
			ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(newStr);
			String materialName = section.getString("material");
			String state = section.getString("state", "unmovable");
			int amount = section.getInt("amount", 1);
			List<Integer> slots = section.getIntegerList("slots");
			List<String> lore = section.getStringList("lore");
			String displayName = section.getString("displayName");
			ItemStack item;
			if (materialName.equals("PLAYER_HEAD"))
				item = ChatPlugin.getSkull(section.getString("texture"));
			else
				item = new ItemStack(Material.valueOf(materialName), amount);

			entries.put(newStr, new Entry(materialName, state, displayName, amount, slots, lore));
			String[] k = newStr.split("\\.");
			String ch = k[k.length - 1];
			System.out.println(ch);
			if (state.equalsIgnoreCase("unmovable"))
				items.put(ch, new ItemSave(ItemState.UNMOVABLE, item));
			else
				items.put(ch, new ItemSave(ItemState.MOVABLE, item));
		});
	}
}