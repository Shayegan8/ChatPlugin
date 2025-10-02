package shayegan8.github.gui;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public record Entry(String materialName, ItemStack item, String displayName, int amount, List<Integer> slots,
		List<String> lore) {
}
