package shayegan8.github.gui;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import lombok.Getter;
import lombok.SneakyThrows;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

enum ItemState {
	UNMOVABLE, MOVABLE
}

record ItemSave(ItemState state, ItemStack item) {

}

record Entry(String materialName, String state, String displayName, int amount, String texture, List<Integer> slots,
		List<String> lore) {
}

public class IMenu implements Listener {

	@Getter
	private Inventory inv;

	private Map<Character, ItemSave> items = new ConcurrentHashMap<Character, ItemSave>();
	private Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();

	@SneakyThrows
	private ItemStack getSkull(String uri) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "foo");
		PlayerTextures textures = profile.getTextures();
		textures.setSkin(URI.create(uri).toURL());
		profile.setTextures(textures);
		meta.setOwnerProfile(profile);
		head.setItemMeta(meta);
		return head;
	}

	public IMenu() {
		inv = Bukkit.createInventory(null, ChatPlugin.configuration_menu.getInt("gui.menu.size", 54));
		ChatPlugin.configuration_menu.getConfigurationSection("gui.menu.list").getKeys(false).forEach(each -> {
			String newStr = "gui.menu.list." + each;
			ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(newStr);
			String materialName = section.getString("material");
			String state = section.getString("state", "unmovable");
			int amount = section.getInt("amount", 1);
			List<Integer> slots = section.getIntegerList("slots");
			List<String> lore = section.getStringList("lore");
			String displayName = section.getString("displayName");
			ItemStack item;
			if (materialName.equals("PLAYER_HEAD")) {
				item = getSkull(section.getString("texture"));
				entries.put(newStr,
						new Entry(materialName, state, displayName, amount, section.getString("texture"), slots, lore));
			} else {
				item = new ItemStack(Material.valueOf(materialName), amount);
				entries.put(newStr, new Entry(materialName, state, displayName, amount, null, slots, lore));
			}
			char ch = newStr.charAt(newStr.length() - 1);
			if (state.equalsIgnoreCase("unmovable"))
				items.put(ch, new ItemSave(ItemState.UNMOVABLE, item));
			else
				items.put(ch, new ItemSave(ItemState.MOVABLE, item));
		});
	}

	public void onPlayerRequest(Player player) {
		entries.entrySet().parallelStream().forEach((entry) -> {
			String key = entry.getKey();
			Entry value = entry.getValue();
			char ch = key.charAt(key.length() - 1);
			Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
				ItemStack item = items.get(ch).item();
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ColorUtils.C(player, value.displayName()));
				meta.setLore(value.lore().parallelStream().map(each -> ColorUtils.C(player, each))
						.collect(Collectors.toList()));
				item.setItemMeta(meta);
				value.slots().forEach(slot -> {
					inv.setItem(slot, item);
				});
			});
		});
	}

}
