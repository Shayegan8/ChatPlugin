package shayegan8.github.events;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;
import shayegan8.github.gui.Gui;

public class GuiListener implements Listener {

	@EventHandler
	public void pages(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		if (!player.getOpenInventory().getTopInventory().equals(ChatPlugin.STORED_MENUINVS.get(player.getUniqueId())))
			return;
		e.setCancelled(true);
		if (e.isRightClick())
			ChatPlugin.iMenu.getEntries().keySet().stream()
					.filter(key -> ChatPlugin.iMenu.getEntries().get(key).item().equals(e.getCurrentItem()))
					.forEach(key -> {
						CompletableFuture<Optional<ConcurrentLinkedDeque<Inventory>>> callback = new CompletableFuture<>();
						System.out.println("clicked key: " + key);
						switch (key) {
						case "gui.menu.list.a":
							MDatabase.getPlayerGroup(player.getUniqueId())
									.thenAccept(group -> ChatPlugin.onPlayerMute(group, true, callback));
							System.out.println("mute callback");
							callback.thenAccept(optInventory -> {
								System.out.println("optional inventory");
								optInventory.ifPresent(inventoryDeque -> {
									System.out.println("this motherfucker is present");
									final Inventory inv = inventoryDeque.getLast();
									player.openInventory(inv);
								});
							});
							break;
						case "gui.menu.list.b":
							player.closeInventory();
							player.performCommand("chatp help");
							break;
						case "gui.menu.list.c":
							player.closeInventory();
							break;
						case "gui.menu.list.d":
							break;
						case "gui.menu.list.e":
							break;
						case "gui.menu.list.f":
							break;
						}
					});
	}

	private void inventoryHandler(InventoryClickEvent e, String groupName, String nextButton, String previousButton,
			Gui gui) {
		final Player player = (Player) e.getWhoClicked();
		final ConcurrentLinkedDeque<Inventory> inventories = ChatPlugin.STORED_MUTEINVS.get(groupName);
		if (inventories == null || inventories.getLast() == null) {
			System.out.println("nulle");
			return;
		}
		Inventory openInventory = player.getOpenInventory().getTopInventory();
		if (!openInventory.equals(inventories.getLast())) {
			System.out.println("mosavi nist");
			return;
		}
		System.out.println("cancelled bitch");
		if (e.isRightClick() || e.isLeftClick()) {
			e.setCancelled(true);
			System.out.println("yes jerk");
			System.out.println(gui.getEntries().size());
			gui.getEntries().entrySet().stream().forEach(entry -> {
				System.out.println("key: " + entry.getKey() + ", value:" + entry.getValue().materialName());
			});
			gui.getEntries().keySet().stream()
					.filter(key -> gui.getEntries().get(key).item().equals(e.getCurrentItem())).forEach(key -> {
						System.out.println(key);
						if (key.equals(nextButton)) {
							System.out.println("next");
							inventoryPage(inventories, player, openInventory, inventories.iterator());
						} else if (key.equals(previousButton)) {
							System.out.println("previous");
							inventoryPage(inventories, player, openInventory, inventories.descendingIterator());
						} else {
							System.out.println("switch handler");
							switchHandler(e, gui, player, nextButton, previousButton);
						}
					});
		}
	}

	private void inventoryPage(ConcurrentLinkedDeque<Inventory> collection, Player player, Inventory openInventory,
			Iterator<Inventory> inventories) {

		CompletableFuture.runAsync(() -> {
			while (inventories.hasNext()) {
				System.out.println("it has next");
				Inventory inventory = inventories.next();
				if (inventory.equals(openInventory)) {
					if (inventories.hasNext()) {
						System.out.println("it has next??");
						Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
								() -> player.openInventory(inventories.next()));
						break;
					} else {
						System.out.println("it hasnt next??");
						ChatPlugin.sendCMSG(player, "chatp.noMorePage", "&cNo other pages found");
						player.closeInventory();
					}
				}
			}
		}, ChatPlugin.EVIRTUAL);
	}

	private void switchHandler(InventoryClickEvent e, Gui gui, Player player, String nextButton,
			String previousButton) {
		ItemStack item = e.getCurrentItem();
		int slot = e.getSlot();
		System.out.println(gui.getName());
		if (gui.getEmpties().contains(slot)) {
			System.out.println("kir khar");
			switch (gui.getName()) {
			case "request":
				player.closeInventory();
				player.performCommand("chatp request " + item.getItemMeta().getDisplayName());
				break;
			case "mute":
				player.closeInventory();
				player.performCommand("chatp mute " + item.getItemMeta().getDisplayName());
				break;
			case "invite":
				player.closeInventory();
				player.performCommand("chatp invite " + item.getItemMeta().getDisplayName());
				break;
			}
		}
	}

	@EventHandler
	public void mute(InventoryClickEvent e) {
		final UUID uuid = ((Player) e.getWhoClicked()).getUniqueId();
		MDatabase.getPlayerGroup(uuid).thenAccept(group -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
				() -> inventoryHandler(e, group, "gui.mute.list.next", "gui.mute.list.previous", ChatPlugin.iMute)));
	}
	
	@EventHandler
	public void delete(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		final Inventory mapCheck = ChatPlugin.STORED_DELETEINVS.get(player.getUniqueId());
		if (mapCheck == null)
			return;
		final Inventory openInventory = player.getOpenInventory().getTopInventory();
		final Inventory inv = ChatPlugin.STORED_DELETEINVS.get(player.getUniqueId());
		if (!openInventory.equals(inv))
			return;
		e.setCancelled(true);
		if (e.isRightClick())
			ChatPlugin.iDelete.getEntries().keySet().stream()
					.filter(key -> ChatPlugin.iDelete.getEntries().get(key).item().equals(e.getCurrentItem()))
					.forEach(key -> {
						switch (key) {
						case "gui.delete.list.true":
							player.closeInventory();
							MDatabase.getPlayerGroup(player.getUniqueId())
									.thenAccept(group -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
											() -> player.performCommand("chatp group delete " + group)));
							break;
						case "gui.delete.list.false":
							player.closeInventory();
							player.performCommand("chatp menu");
							break;
						}
					});
	}

}
