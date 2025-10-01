package shayegan8.github.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import shayegan8.github.ChatPlugin;

public class GuiListener implements Listener {

	@EventHandler
	public void inv(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		if (!inv.equals(ChatPlugin.iMenu.getInv()))
			return;
		boolean check = ChatPlugin.iMenu.getItems().values().stream()
				.anyMatch(itemS -> itemS.state() == ItemState.UNMOVABLE && e.getCurrentItem().equals(itemS.item()));
		if (check)
			e.setCancelled(true);
	}

	@EventHandler
	public void pages(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		ChatPlugin.iMenu.getItems().entrySet().stream().forEach(entry -> {
			switch (entry.getKey()) {
			case "a":
				
				break;
			}
		});
	}

}
