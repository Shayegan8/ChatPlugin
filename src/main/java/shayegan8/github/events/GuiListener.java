package shayegan8.github.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import shayegan8.github.ChatPlugin;

public class GuiListener implements Listener {

	@EventHandler
	public void pages(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		if (!player.getOpenInventory().getTopInventory().equals(ChatPlugin.iMenu.getInv()))
			return;
		e.setCancelled(true);
		if (e.isRightClick())
			ChatPlugin.iMenu.getEntries().keySet().stream()
					.filter(key -> ChatPlugin.iMenu.getEntries().get(key).item().equals(e.getCurrentItem()))
					.forEach(key -> {
						System.out.println(key);
						switch (key) {
						case "gui.menu.list.a":
							ChatPlugin.onPlayerMuteWithSkulls(player, ChatPlugin.iMute.getEntries(),
									ChatPlugin.iMute.getInv());
							player.openInventory(ChatPlugin.iMute.getInv());
							break;
						}
					});
	}

}
