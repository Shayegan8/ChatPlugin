package shayegan8.github.gui;

import java.util.List;

public record Entry(String materialName, String state, String displayName, int amount, List<Integer> slots,
		List<String> lore) {
}
