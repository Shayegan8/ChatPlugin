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
							ChatPlugin.onPlayerMute(player);
							player.openInventory(ChatPlugin.STORED_INVS.get(0));
							break;
						}
					});
	}

	@EventHandler
	public void onMute(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		if (!player.getOpenInventory().getTopInventory().equals(ChatPlugin.STORED_INVS.get(0)))
			return;
		e.setCancelled(true);
		if (e.isRightClick())
			ChatPlugin.iMute.getEntries().keySet().stream()
					.filter(key -> ChatPlugin.iMute.getEntries().get(key).item().equals(e.getCurrentItem()))
					.forEach(key -> {
						switch (key) {
						case "gui.mute.list.next":
							ChatPlugin.STORED_INVS.forEach((slot, inv) -> {
								if (ChatPlugin.STORED_INVS.get(slot++) == null) {
									player.closeInventory();
									ChatPlugin.sendCMSG(player, "chatp.gmute.notFound", "&cNo more page is valid");
									return;
								}
								player.openInventory(ChatPlugin.STORED_INVS.get(slot++));
							});
							break;
						}
					});
	}

}
