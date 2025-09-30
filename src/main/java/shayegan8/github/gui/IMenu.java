package shayegan8.github.gui;

import java.lang.reflect.Field;
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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import lombok.SneakyThrows;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

public class IMenu implements Listener {

	public static Inventory inv;

	public static Map<Character, ItemStack> items = new ConcurrentHashMap<Character, ItemStack>();

	@SneakyThrows
	private static ItemStack getSkull(String base64num) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", base64num));
		Field profileF = meta.getClass().getDeclaredField("profile");
		profileF.setAccessible(true);
		profileF.set(meta, profileF);
		head.setItemMeta(meta);
		return head;
	}

	public IMenu() {
		inv = Bukkit.createInventory(null, ChatPlugin.configuration_menu.getInt("gui.menu.size", 54));
		ChatPlugin.configuration_menu.getConfigurationSection("gui.menu.list").getKeys(false).forEach(each -> {
			ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(each);
			String materialName = section.getString("material");
			ItemStack item;
			int amount = section.getInt("amount", 1);
			if (materialName.equals("PLAYER_HEAD"))
				item = getSkull(section.getString("texture"));
			else
				item = new ItemStack(Material.valueOf(materialName), amount);
			char ch = each.charAt(each.length() - 1);
			items.put(ch, item);
		});
	}

	public static void onPlayerRequest(Player player) {
		ChatPlugin.configuration_menu.getConfigurationSection("gui.menu.list").getKeys(false).forEach(each -> {
			char ch = each.charAt(each.length() - 1);
			ItemStack item = items.get(ch);
			ItemMeta meta = item.getItemMeta();
			ConfigurationSection section = ChatPlugin.configuration_menu.getConfigurationSection(each);
			String displayName = ColorUtils.C(player, section.getString("displayName"));
			List<String> lore = section.getStringList("lore").stream().map(e -> ColorUtils.C(player, e))
					.collect(Collectors.toList());
			meta.setDisplayName(displayName);
			meta.setLore(lore);
			item.setItemMeta(meta);
			Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> item.setItemMeta(meta));
			List<Integer> slots = section.getIntegerList("slots");
			Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
				slots.forEach(slot -> {
					inv.setItem(slot, item);
				});
			});
		});
	}

}
