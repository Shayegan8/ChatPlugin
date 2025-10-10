package shayegan8.github.gui;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Entry {
	
	private final String materialName;
	private final ItemStack item;
	private final String displayName;
	private final int amount;
	private final List<Integer> slots;
	private final List<String> lore;
}
