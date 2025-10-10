package shayegan8.github.events;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
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

public class GuiListener implements Listener {

	@EventHandler
	public void onMove(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		final Inventory currentInventory = player.getOpenInventory().getTopInventory();
		if (currentInventory == null)
			return;
		if (ChatPlugin.STATES.contains(currentInventory))
			e.setCancelled(true);
	}

	@EventHandler
	public void openMenu(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		final Inventory currentInventory = player.getOpenInventory().getTopInventory();
		final ItemStack currentItem = e.getCurrentItem();
		MDatabase.getPlayerGroup(player.getUniqueId()).thenAccept(group -> {
			Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
				if (currentInventory == null)
					return;
				if (ChatPlugin.storedMenu.equals(currentInventory)) {
					ChatPlugin.iMenu.getEntries().entrySet().stream()
							.filter(entry -> entry.getValue().getItem().equals(currentItem)).forEach(entry -> {
								switch (entry.getKey()) {
								case "gui.menu.list.c":
									player.closeInventory();
									break;
								case "gui.menu.list.b":
									player.performCommand("chatp help");
									player.closeInventory();
									break;
								case "gui.menu.list.a":
									CompletableFuture<ConcurrentLinkedDeque<Inventory>> completeAss = new CompletableFuture<>();
									ChatPlugin.onPlayerMute(player, completeAss, false);
									completeAss.thenAccept(inventory -> {
										Bukkit.getScheduler().runTask(ChatPlugin.getInstance(),
												() -> player.openInventory(inventory.getLast()));
									});
									break;
								case "gui.menu.list.d":
									CompletableFuture<Inventory> completeInv = new CompletableFuture<>();
									System.out.println("inventory future");
									ChatPlugin.onPlayerInvite(completeInv, false);
									System.out.println("onPlayerInvite");
									completeInv.thenAccept(
											inventory -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
												if (inventory != null) {
													System.out.println("opening inventory");
													player.openInventory(inventory);
												}
											}));
								case "gui.menu.list.e":
									CompletableFuture<Inventory> completeInv2 = new CompletableFuture<>();
									ChatPlugin.onPlayerRequest(completeInv2, false);
									completeInv2.thenAccept(
											inventory -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
												if (inventory != null)
													player.openInventory(inventory);
											}));
								case "gui.menu.list.f":
									player.openInventory(ChatPlugin.onPlayerDelete());
								}
							});
				} else if (ChatPlugin.STORED_MUTEINVS.get(group) != null
						&& ChatPlugin.STORED_MUTEINVS.get(group).contains(currentInventory)) {
					ChatPlugin.iMute.getEntries().entrySet().stream()
							.filter(entry -> entry.getValue().getItem().equals(currentItem)).forEach(entry -> {
								final String key = entry.getKey();
								if (key.equals("gui.mute.list.next")) {
									Iterator<Inventory> incIterator = ChatPlugin.STORED_MUTEINVS.get(group)
											.descendingIterator();
									iterationInfunc(incIterator, currentInventory, player);
								} else if (key.equals("gui.mute.list.previous")) {
									Iterator<Inventory> decIterator = ChatPlugin.STORED_MUTEINVS.get(group).iterator();
									iterationInfunc(decIterator, currentInventory, player);
								} else if (entry.getValue().getItem().getType() == Material.PLAYER_HEAD) {
									player.performCommand("chatp mute " + entry.getKey());
									player.closeInventory();
								}
							});
				} else if (ChatPlugin.STORED_INVITEINVS.containsValue(currentInventory)) {
					ChatPlugin.STORED_INVITEINVS.entrySet().stream()
							.filter(entry_ -> entry_.getValue().equals(currentInventory)).forEach(entry_ -> {
								final int currentIndex = entry_.getKey();
								ChatPlugin.iInvite.getEntries().entrySet().stream()
										.filter(entry -> entry.getValue().getItem().equals(currentItem)).forEach(entry -> {
											final String key = entry.getKey();
											if (key.equals("gui.invite.list.next")) {
												if (ChatPlugin.STORED_INVITEINVS.containsKey(currentIndex + 1))
													player.openInventory(
															ChatPlugin.STORED_INVITEINVS.get(currentIndex + 1));
												else {
													ChatPlugin.sendACMSG(player, "chatp.invite.noMorePage",
															"&cThere is no more page");
													player.closeInventory();
												}
											} else if (key.equals("gui.invite.list.previous")) {
												if (ChatPlugin.STORED_INVITEINVS.containsKey(currentIndex - 1))
													player.openInventory(
															ChatPlugin.STORED_INVITEINVS.get(currentIndex - 1));
												else {
													ChatPlugin.sendACMSG(player, "chatp.invite.noMorePage",
															"&cThere is no more page");
													player.closeInventory();
												}
											} else if (entry.getValue().getItem().getType() == Material.PLAYER_HEAD) {
												player.performCommand("chatp invite " + entry.getKey());
												player.closeInventory();
											}
										});
							});
				} else if (ChatPlugin.STORED_REQUESTINVS.containsValue(currentInventory)) {
					ChatPlugin.STORED_REQUESTINVS.entrySet().stream()
							.filter(entry_ -> entry_.getValue().equals(currentInventory)).forEach(entry_ -> {
								final int currentIndex = entry_.getKey();
								ChatPlugin.iRequest.getEntries().entrySet().stream()
										.filter(entry -> entry.getValue().getItem().equals(currentItem)).forEach(entry -> {
											final String key = entry.getKey();
											if (key.equals("gui.request.list.next")) {
												if (ChatPlugin.STORED_REQUESTINVS.containsKey(currentIndex + 1))
													player.openInventory(
															ChatPlugin.STORED_REQUESTINVS.get(currentIndex + 1));
												else {
													ChatPlugin.sendACMSG(player, "chatp.request.noMorePage",
															"&cThere is no more page");
													player.closeInventory();
												}
											} else if (key.equals("gui.request.list.previous")) {
												if (ChatPlugin.STORED_REQUESTINVS.containsKey(currentIndex - 1))
													player.openInventory(
															ChatPlugin.STORED_REQUESTINVS.get(currentIndex - 1));
												else {
													ChatPlugin.sendACMSG(player, "chatp.request.noMorePage",
															"&cThere is no more page");
													player.closeInventory();
												}
											} else if (entry.getValue().getItem().getType() == Material.PLAYER_HEAD) {
												player.performCommand("chatp request " + entry.getKey());
												player.closeInventory();
											}
										});
							});
				} else if (ChatPlugin.storedDelete.equals(currentInventory)) {
					ChatPlugin.iDelete.getEntries().entrySet().stream()
							.filter(entry -> entry.getValue().getItem().equals(currentItem)).forEach(entry -> {
								final String key = entry.getKey();
								switch (key) {
								case "gui.delete.list.true":
									player.performCommand("chatp group delete " + group);
									player.closeInventory();
								case "gui.delete.list.false":
									player.closeInventory();
								}
							});
				}

			});
		});
	}

	private void iterationInfunc(Iterator<Inventory> iterator, Inventory currentInventory, Player player) {
		if (iterator.hasNext()) {
			Inventory nextInv = iterator.next();
			if (nextInv.equals(currentInventory))
				if (iterator.hasNext())
					player.openInventory(iterator.next());
				else {
					ChatPlugin.sendACMSG(player, "chatp.mute.noMorePage", "&cThere is no more page");
					player.closeInventory();
				}
			else {
				iterator.next();
				iterationInfunc(iterator, currentInventory, player);
			}
		}
	}

}
