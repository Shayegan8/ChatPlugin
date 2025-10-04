package shayegan8.github.events;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
						System.out.println(key);
						CompletableFuture<Inventory> callback = new CompletableFuture<>();
						switch (key) {
						case "gui.menu.list.a":
							ChatPlugin.onPlayerMute(player, callback);
							callback.thenAccept(firstInventory -> {
								Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
										() -> player.openInventory(firstInventory));
							});
							System.out.println("3");
							break;
						case "gui.menu.list.b":
							player.closeInventory();
							player.performCommand("chatp help");
							break;
						case "gui.menu.list.c":
							player.closeInventory();
							break;
						case "gui.menu.list.d":
							ChatPlugin.onPlayerInvite(player, callback);
							callback.thenAccept(firstInventory -> {
								Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
										() -> player.openInventory(firstInventory));
							});
							break;
						case "gui.menu.list.e":
							ChatPlugin.onPlayerRequest(player, callback);
							callback.thenAccept(firstInventory -> {
								Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
										() -> player.openInventory(firstInventory));
							});
							break;
						case "gui.menu.list.f":
							ChatPlugin.onPlayerDelete(player, callback);
							break;
						}
					});
	}

	private void inventoryHandler(InventoryClickEvent e, String nextButton, String previousButton,
			ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Inventory>> collection, Gui gui) {
		final Player player = (Player) e.getWhoClicked();
		final ConcurrentHashMap<Integer, Inventory> mapCheck = collection.get(player.getUniqueId());
		if (mapCheck == null || mapCheck.get(0) == null)
			return;
		final Inventory openInventory = player.getOpenInventory().getTopInventory();
		final Inventory inv = collection.get(player.getUniqueId()).get(0);
		if (!openInventory.equals(inv))
			return;
		e.setCancelled(true);
		if (e.isRightClick()) {
			gui.getEntries().keySet().stream()
					.filter(key -> gui.getEntries().get(key).item().equals(e.getCurrentItem())).forEach(key -> {
						System.out.println(key);
						if (key.equalsIgnoreCase(nextButton)) {
							AtomicInteger atomI = new AtomicInteger(0);
							final List<ConcurrentHashMap<Integer, Inventory>> result = collection.values().stream()
									.filter(entry -> entry.get(atomI.getAndIncrement()).equals(openInventory))
									.collect(Collectors.toList());
							result.getFirst().entrySet().stream().forEach(entry -> {
								int currentIndex = entry.getKey();
								Optional<Inventory> currentInv = Optional
										.ofNullable(collection.get(player.getUniqueId()).get(currentIndex + 1));
								currentInv.ifPresentOrElse(inventory -> {
									player.openInventory(inventory);
								}, () -> {
									player.closeInventory();
								});
							});
						} else if (key.equalsIgnoreCase(previousButton)) {
							AtomicInteger atomI = new AtomicInteger(0);
							final List<ConcurrentHashMap<Integer, Inventory>> result = collection.values().stream()
									.filter(entry -> entry.get(atomI.getAndIncrement()).equals(openInventory))
									.collect(Collectors.toList());
							result.getFirst().entrySet().stream().forEach(entry -> {
								int currentIndex = entry.getKey();
								Optional<Inventory> currentInv = Optional
										.ofNullable(collection.get(player.getUniqueId()).get(currentIndex - 1));
								currentInv.ifPresentOrElse(inventory -> {
									player.openInventory(inventory);
								}, () -> {

									player.closeInventory();
								});
							});
						}
					});
			if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
				ItemStack item = e.getCurrentItem();
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
	}

	@EventHandler
	public void mute(InventoryClickEvent e) {
		inventoryHandler(e, "gui.mute.list.next", "gui.mute.list.previous", ChatPlugin.STORED_MUTEINVS,
				ChatPlugin.iMute);
	}

	@EventHandler
	public void invite(InventoryClickEvent e) {
		inventoryHandler(e, "gui.invite.list.next", "gui.invite.list.previous", ChatPlugin.STORED_INVITEINVS,
				ChatPlugin.iInvite);
	}

	@EventHandler
	public void request(InventoryClickEvent e) {
		inventoryHandler(e, "gui.request.list.next", "gui.request.list.previous", ChatPlugin.STORED_REQUESTINVS,
				ChatPlugin.iRequest);
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
