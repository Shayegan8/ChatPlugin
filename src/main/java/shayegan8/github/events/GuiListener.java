package shayegan8.github.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import shayegan8.github.ChatPlugin;

public class GuiListener implements Listener {

	public void inv(InventoryDragEvent e) {
		Player player = (Player) e.getWhoClicked();
		if (player.getOpenInventory().getTopInventory().equals(ChatPlugin.iMute.getInv()))
			e.setCancelled(true);
	}

	@EventHandler
	public void pages(InventoryClickEvent e) {
		ChatPlugin.iMenu.getEntries().entrySet().stream().forEach(entry -> {
			switch (entry.getKey()) {
			case "chatp.mute.list.a":

				break;
			}
		});
	}

}
