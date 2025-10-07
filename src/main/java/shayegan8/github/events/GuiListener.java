package shayegan8.github.events;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import shayegan8.github.ChatPlugin;

public class GuiListener implements Listener {

	private final static AtomicBoolean CONTAIN = new AtomicBoolean(false);

	@EventHandler
	public void openMenu(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		final Inventory currentInventory = e.getClickedInventory();
		final ItemStack currentItem = e.getCurrentItem();
		CONTAIN.lazySet(false);
		ChatPlugin.STORED_MUTEINVS.values().forEach(eachValue -> {
			if (eachValue.contains(currentInventory))
				CONTAIN.lazySet(true);
		});
		if (!(ChatPlugin.storedMenu.equals(currentInventory) || ChatPlugin.storedDelete.equals(currentInventory)
				|| ChatPlugin.STORED_REQUESTINVS.containsValue(currentInventory)
				|| ChatPlugin.STORED_INVITEINVS.containsValue(currentInventory)) || CONTAIN.get()) {
			return;
		}
		e.setCancelled(true);
		CompletableFuture<ConcurrentLinkedDeque<Inventory>> completeAss = new CompletableFuture<>();
		if (ChatPlugin.storedMenu.equals(currentInventory)) {
			ChatPlugin.iMenu.getEntries().entrySet().stream()
					.filter(entry -> entry.getValue().item().equals(currentItem)).forEach(entry -> {
						System.out.println(entry.getKey());
						switch (entry.getKey()) {
						case "gui.menu.list.c":
							player.closeInventory();
							break;
						case "gui.menu.list.b":
							player.performCommand("chatp help");
							player.closeInventory();
							break;
						case "gui.menu.list.a":
							ChatPlugin.onPlayerMute(player, completeAss, false);
							completeAss.thenAccept(action -> {
								Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
										() -> player.openInventory(action.getFirst()));
							});
						}
					});
		}
	}

	@EventHandler
	public void openKir(InventoryDragEvent e) {
		final Inventory currentInventory = e.getWhoClicked().getOpenInventory().getTopInventory();
		if (!(ChatPlugin.storedMenu.equals(currentInventory) || ChatPlugin.storedDelete.equals(currentInventory)
				|| ChatPlugin.STORED_REQUESTINVS.containsValue(currentInventory)
				|| ChatPlugin.STORED_INVITEINVS.containsValue(currentInventory)) || CONTAIN.get()) {
			return;
		}
		e.setCancelled(true);
	}

}
