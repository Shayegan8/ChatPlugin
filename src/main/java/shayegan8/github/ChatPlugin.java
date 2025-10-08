package shayegan8.github;

import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import shayegan8.github.commands.*;
import shayegan8.github.database.MDatabase;
import shayegan8.github.events.*;
import shayegan8.github.expansions.Placeholders;
import shayegan8.github.gui.Entry;
import shayegan8.github.gui.IDelete;
import shayegan8.github.gui.IInvite;
import shayegan8.github.gui.IMenu;
import shayegan8.github.gui.IMute;
import shayegan8.github.gui.IRequest;
import shayegan8.github.gui.IState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Plugin(name = "ChatPlugin", version = "1.0.0")
@Description("Simple chat plugin :O")
@Author("Shayegan8")
@Permission(name = "chatp.base.help", desc = "chatplugin help command")
@Permission(name = "chatp.base.reload", desc = "chatplugin reload command")
@Permission(name = "chatp.base.remove", desc = "Chatplugin remove permission")
@Permission(name = "chatp.base.quit", desc = "Chatplugin quit permission")
@Permission(name = "chatp.base.mute", desc = "Chatplugin mute permission")
@Permission(name = "chatp.base.menu", desc = "Chatplugin menu permission")
@Permission(name = "chatp.base.join", desc = "Chatplugin join permission")
@Permission(name = "chatp.base.invite", desc = "Chatplugin invite permission")
@Permission(name = "chatp.base.group", desc = "Chatplugin group permission")
@Permission(name = "chatp.base.accept", desc = "Chatplugin accept permission")
@Permission(name = "chatp.base.request", desc = "Chatplugin request permission")
@Permission(name = "chatp.base.*", desc = "Chatplugin wildcard permission")
@Permission(name = "chatp.base", desc = "Chatplugin base permission")
@Commands({ @Command(name = "chatp", desc = "chatplugin base command", permission = "chatp.base", usage = "/chatp") })
@ApiVersion(Target.v1_13)
@SoftDependency("PlaceholderAPI")
public final class ChatPlugin extends JavaPlugin {

	private static File file_;
	private static File file2_;
	public static FileConfiguration configuration;
	public static FileConfiguration configuration_menu;
	public final static Map<String, CommandManager> commands = Map.ofEntries(Map.entry("group", new Group()),
			Map.entry("help", new Help()), Map.entry("invite", new Invite()), Map.entry("join", new Join()),
			Map.entry("mute", new Mute()), Map.entry("quit", new Quit()), Map.entry("reload", new ReloadC()),
			Map.entry("remove", new Remove()), Map.entry("staff", new Staff()), Map.entry("menu", new CMenu()),
			Map.entry("request", new Request()), Map.entry("accept", new Accept()));
	public final static Map<String, Integer> tags = Map.of("none", 1, "staff", 2, "admin", 3);
	public static Map<String, Object> entries;
	public static MDatabase mDB;
	private static final String GREEN = "\u001b[32m";
	private static final String RED = "\u001b[31m";
	private static final String REFRESH = "\u001b[0m";
	private static final String URL = "";
	public static final ExecutorService EVIRTUAL = Executors.newVirtualThreadPerTaskExecutor();
	private FileOutputStream fout;
	public static IMute iMute;
	public static IMenu iMenu;
	public static IInvite iInvite;
	public static IRequest iRequest;
	public static IDelete iDelete;
	private static ChatPlugin plugin;

	public static void sendBMSG(CommandSender sender, String path, String msg) {
		Bukkit.getScheduler().runTask(getInstance(),
				() -> sender.sendMessage(ColorUtils.B((String) entries.getOrDefault(path, msg))));
	}

	public static void sendCMSG(Player player, String path, String msg) {
		Bukkit.getScheduler().runTask(getInstance(), () -> player.sendMessage(PlaceholderAPI.setPlaceholders(player,
				ColorUtils.C(player, (String) entries.getOrDefault(path, msg)))));
	}

	public static void sendABMSG(CommandSender sender, String path, String msg) {
		sender.sendMessage(ColorUtils.B((String) ChatPlugin.entries.getOrDefault(path, msg)));
	}

	public static void sendACMSG(Player player, String path, String msg) {
		player.sendMessage(ColorUtils.C(player, (String) ChatPlugin.entries.getOrDefault(path, msg)));
	}

	public static Map<String, Object> sectionSaver() {
		ConfigurationSection section = configuration.getConfigurationSection("chatp");
		entries = new ConcurrentHashMap<String, Object>();
		section.getKeys(true).forEach(each -> {
			String key = "chatp." + each;
			Object eachSection = configuration.get(key);
			entries.put(key, eachSection);
		});
		return Collections.unmodifiableMap(entries);
	}

	public static ChatPlugin getInstance() {
		return plugin;
	}

	public static Inventory storedMenu;
	public static Inventory storedDelete;
	private static final AtomicBoolean CALL_ONCEMENU = new AtomicBoolean(false);
	private static final AtomicBoolean CALL_ONCEDELETE = new AtomicBoolean(false);

	@SneakyThrows
	@Override
	public void onEnable() {
		plugin = this;
		getLogger().info("Configuration...");
		createConfig();
		entries = sectionSaver();
		getLogger().info("Registering commands...");
		this.getCommand("chatp").setExecutor(new BaseCommand(new CooldownManager()));
		this.getCommand("chatp").setTabCompleter(new BaseCommand(new CooldownManager()));
		getLogger().info("Establishing connection to database...");
		mDB = new MDatabase(configuration.getString("dbName", "sqlite"));
		getLogger().info("Registering guis...");
		iMenu = new IMenu();
		storedMenu = Bukkit.createInventory(null, iMenu.getSize(), iMenu.getTitle());
		iDelete = new IDelete();
		storedDelete = Bukkit.createInventory(null, iDelete.getSize(), iDelete.getTitle());
		iMute = new IMute();
		iInvite = new IInvite();
		iRequest = new IRequest();
		getLogger().info("Registering events...");
		getServer().getPluginManager().registerEvents(new Grouping(), this);
		getServer().getPluginManager().registerEvents(new GuiListener(), this);
		if (configuration.getBoolean("update", false)) {
			try {
				getLogger().info("Update is enabled...");
				ReadableByteChannel channel = Channels.newChannel(new URI(URL).toURL().openStream());
				Path path = Path.of("plugins/update");
				if (Files.notExists(path))
					Files.createDirectory(path);
				fout = new FileOutputStream("plugins/updates/");
				FileChannel fc = fout.getChannel();
				getLogger().info("Downloading...");
				fc.transferFrom(channel, 0, Long.MAX_VALUE);
			} finally {
				fout.close();
			}
		}
		getLogger().info("Registering placeholders (PlaceholderAPI)");
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new Placeholders(this).register();
			getLogger().info(GREEN + "ChatPlugin enabled" + REFRESH);
		} else {
			getLogger().info(RED + "Couldn't find PlaceholderAPI, disabling plugin..." + REFRESH);
			getPluginLoader().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		try {
			if (mDB.getConnection() != null)
				mDB.getConnection().close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		EVIRTUAL.shutdown();
		getLogger().info(RED + "ChatPlugin disabled" + REFRESH);
	}

	@SneakyThrows
	public static ItemStack getSkull(String uri) {
		final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		final SkullMeta meta = (SkullMeta) head.getItemMeta();
		final PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "foo");
		final PlayerTextures textures = profile.getTextures();
		textures.setSkin(URI.create(uri).toURL());
		profile.setTextures(textures);
		meta.setOwnerProfile(profile);
		head.setItemMeta(meta);
		return head;
	}

	public static ItemStack getSkullOfOwner(Player player) {
		final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		final SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwnerProfile(player.getPlayerProfile());
		head.setItemMeta(meta);
		return head;
	}

	public static Inventory onPlayerMenu() {
		if (CALL_ONCEMENU.get())
			return storedMenu;
		normalFiller(iMenu.getEntries(), storedMenu);
		STATES.put(storedMenu, IState.IMPORTANT);
		CALL_ONCEMENU.lazySet(true);
		return storedMenu;
	}

	private static void normalFiller(Map<String, Entry> entries, Inventory inv) {
		entries.entrySet().stream().forEach((entry) -> {
			final Entry value = entry.getValue();
			final ItemStack item = value.item();
			value.slots().forEach(slot -> {
				Bukkit.getScheduler().runTask(getInstance(), () -> {
					final ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(ColorUtils.B(value.displayName()));
					meta.setLore(value.lore().stream().map(each -> ColorUtils.B(each)).collect(Collectors.toList()));
					item.setItemMeta(meta);
					inv.setItem(slot, item);
				});
			});
		});
	}

	public static Inventory onPlayerDelete() {
		if (CALL_ONCEDELETE.get())
			return storedDelete;
		normalFiller(iDelete.getEntries(), storedDelete);
		STATES.put(storedDelete, IState.IMPORTANT);
		CALL_ONCEDELETE.lazySet(true);
		return storedDelete;
	}

	public static final ConcurrentHashMap<Inventory, IState> STATES = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, ConcurrentLinkedDeque<Inventory>> STORED_MUTEINVS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Inventory> STORED_INVITEINVS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Inventory> STORED_REQUESTINVS = new ConcurrentHashMap<>();

	private static void repeatIteration(List<Integer> empties, List<UUID> cloneList, Inventory inventory) {
		normalFiller(iMute.getEntries(), inventory);
		CompletableFuture.runAsync(() -> {
			Iterator<Integer> iterator = empties.iterator();
			for (UUID eachUUID : cloneList)
				if (iterator.hasNext())
					Bukkit.getScheduler().runTask(plugin, () -> {
						if (Bukkit.getPlayer(eachUUID) == null)
							return;
						final Player skullPlayer = Bukkit.getPlayer(eachUUID);
						final ItemStack item = getSkullOfOwner(skullPlayer);
						final ItemMeta meta = item.getItemMeta();
						meta.setLore(iMute.getLore().stream().map(each -> ColorUtils.B(each))
								.collect(Collectors.toUnmodifiableList()));
						meta.setDisplayName(ColorUtils.B("&7" + skullPlayer.getName()));
						item.setItemMeta(meta);
						final int slot = iterator.next();
						inventory.setItem(slot, item);
						iMute.getEntries().put(skullPlayer.getName(), new Entry(null, item, null, 0, null, null));
					});
		}, EVIRTUAL);
	}

	private static void onPlayerRepeat(String groupName, List<UUID> cloneList, List<Integer> empties,
			AtomicInteger checkingArea, int emptySlots, CompletableFuture<ConcurrentLinkedDeque<Inventory>> callback) {
		final ConcurrentLinkedDeque<Inventory> inventories = new ConcurrentLinkedDeque<Inventory>();
		Inventory inventory = Bukkit.createInventory(null, iMute.getSize(), iMute.getTitle());
		if (checkingArea.get() < emptySlots) {
			repeatIteration(empties, cloneList, inventory);
			Bukkit.getScheduler().runTask(plugin, () -> {
				inventories.offer(inventory);
				STATES.put(inventory, IState.IMPORTANT);
				STORED_MUTEINVS.put(groupName, inventories);
				callback.complete(STORED_MUTEINVS.get(groupName));
			});
		} else {
			repeatIteration(empties, cloneList, inventory);
			for (int remove = 1; remove < emptySlots; remove++)
				cloneList.removeLast();
			Bukkit.getScheduler().runTask(plugin, () -> {
				checkingArea.lazySet(checkingArea.get() - emptySlots);
				inventories.offer(inventory);
				STATES.put(inventory, IState.IMPORTANT);
			});
			onPlayerRepeat(groupName, cloneList, empties, checkingArea, emptySlots, callback);
		}
	}

	public static void onPlayerMute(Player player, CompletableFuture<ConcurrentLinkedDeque<Inventory>> callback,
			boolean update) {
		MDatabase.getPlayerGroup(player.getUniqueId()).thenAccept(group -> {
			final Optional<ConcurrentLinkedDeque<Inventory>> opt = Optional.ofNullable(STORED_MUTEINVS.get(group));
			if (opt.isPresent() && !update) {
				callback.complete(opt.get());
			} else {
				MDatabase.getPlayersByGroup(group).thenAccept(players -> {
					final List<UUID> cloneList = players;
					final int playersSize = cloneList.size();
					final List<Integer> empties = Collections.unmodifiableList(iMute.getEmpties());
					final int emptySlots = empties.size();
					AtomicInteger checkingArea = new AtomicInteger(playersSize - emptySlots);
					onPlayerRepeat(group, cloneList, empties, checkingArea, emptySlots, callback);
				});
			}
		});
	}

	private static void repeatIterationRequest(List<Integer> empties, List<String> cloneList, Inventory inventory) {
		normalFiller(iRequest.getEntries(), inventory);
		CompletableFuture.runAsync(() -> {
			Iterator<Integer> iterator = empties.iterator();
			for (String eachGroup : cloneList)
				if (!eachGroup.equals("none"))
					if (iterator.hasNext()) {
						MDatabase.getGroupAdmin(eachGroup)
								.thenAccept(adminUUID -> Bukkit.getScheduler().runTask(getInstance(), () -> {
									final Player skullPlayer = Bukkit.getPlayer(adminUUID);
									if (!skullPlayer.isOnline())
										return;
									final ItemStack item = getSkullOfOwner(skullPlayer);
									final ItemMeta meta = item.getItemMeta();
									meta.setLore(iRequest.getLore().stream().map(each -> ColorUtils.B(each))
											.collect(Collectors.toUnmodifiableList()));
									meta.setDisplayName(ColorUtils.B("&7" + skullPlayer.getName()));
									item.setItemMeta(meta);
									final int slot = iterator.next();
									inventory.setItem(slot, item);
									iRequest.getEntries().put(skullPlayer.getName(),
											new Entry(null, item, null, 0, null, null));
								}));
					}
		}, EVIRTUAL);
	}

	private static void onPlayerRepeatRequest(AtomicInteger pageNumber, List<String> cloneList, List<Integer> empties,
			AtomicInteger checkingArea, int emptySlots, CompletableFuture<Inventory> callback) {
		Inventory inventory = Bukkit.createInventory(null, iRequest.getSize(), iRequest.getTitle());
		if (checkingArea.get() < emptySlots) {
			repeatIterationRequest(empties, cloneList, inventory);
			Bukkit.getScheduler().runTask(plugin, () -> {
				STORED_REQUESTINVS.put(pageNumber.get(), inventory);
				STATES.put(inventory, IState.IMPORTANT);
				callback.complete(STORED_REQUESTINVS.get(0));
			});
		} else {
			repeatIterationRequest(empties, cloneList, inventory);
			for (int remove = 1; remove < emptySlots; remove++)
				cloneList.removeLast();
			Bukkit.getScheduler().runTask(plugin, () -> {
				checkingArea.lazySet(checkingArea.get() - emptySlots);
				STATES.put(inventory, IState.IMPORTANT);
				STORED_REQUESTINVS.put(pageNumber.getAndIncrement(), inventory);
			});
			onPlayerRepeatRequest(pageNumber, cloneList, empties, checkingArea, emptySlots, callback);
		}
	}

	public static void onPlayerRequest(CompletableFuture<Inventory> callback, boolean update) {
		MDatabase.getGroupList().thenAccept(groupList -> {
			if (!STORED_REQUESTINVS.isEmpty() && !update)
				callback.complete(STORED_REQUESTINVS.get(0));
			else {
				final List<String> cloneList = groupList;
				final int playersSize = cloneList.size();
				final List<Integer> empties = Collections.unmodifiableList(iRequest.getEmpties());
				final int emptySlots = empties.size();
				AtomicInteger checkingArea = new AtomicInteger(playersSize - emptySlots);
				AtomicInteger pageNumber = new AtomicInteger(0);
				onPlayerRepeatRequest(pageNumber, cloneList, empties, checkingArea, emptySlots, callback);
			}
		});
	}

	private static void repeatIterationInvite(List<Integer> empties, List<Player> cloneList, Inventory inventory) {
		normalFiller(iInvite.getEntries(), inventory);
		CompletableFuture.runAsync(() -> {
			Iterator<Integer> iterator = empties.iterator();
			for (final Player eachPlayer : cloneList)
				if (iterator.hasNext())
					Bukkit.getScheduler().runTask(plugin, () -> {
						if (!eachPlayer.isOnline())
							return;
						final ItemStack item = getSkullOfOwner(eachPlayer);
						final ItemMeta meta = item.getItemMeta();
						meta.setLore(iInvite.getLore().stream().map(each -> ColorUtils.B(each))
								.collect(Collectors.toUnmodifiableList()));
						meta.setDisplayName(ColorUtils.B("&7" + eachPlayer.getName()));
						item.setItemMeta(meta);
						final int slot = iterator.next();
						inventory.setItem(slot, item);
						iInvite.getEntries().put(eachPlayer.getName(), new Entry(null, item, null, 0, null, null));
					});
		}, EVIRTUAL);
	}

	private static void onPlayerRepeatInvite(AtomicInteger pageNumber, List<Player> cloneList, List<Integer> empties,
			AtomicInteger checkingArea, int emptySlots, CompletableFuture<Inventory> callback) {
		Inventory inventory = Bukkit.createInventory(null, iInvite.getSize(), iInvite.getTitle());
		if (checkingArea.get() < emptySlots) {
			repeatIterationInvite(empties, cloneList, inventory);
			Bukkit.getScheduler().runTask(plugin, () -> {
				STATES.put(inventory, IState.IMPORTANT);
				STORED_INVITEINVS.put(pageNumber.incrementAndGet(), inventory);
				callback.complete(STORED_INVITEINVS.get(0));
			});
		} else {
			repeatIterationInvite(empties, cloneList, inventory);
			for (int remove = 1; remove < emptySlots; remove++)
				cloneList.removeLast();
			Bukkit.getScheduler().runTask(plugin, () -> {
				checkingArea.lazySet(checkingArea.get() - emptySlots);
				STATES.put(inventory, IState.IMPORTANT);
				STORED_INVITEINVS.put(pageNumber.getAndIncrement(), inventory);
			});
			onPlayerRepeatInvite(pageNumber, cloneList, empties, checkingArea, emptySlots, callback);
		}
	}

	public static void onPlayerInvite(CompletableFuture<Inventory> callback, boolean update) {
		if (!STORED_INVITEINVS.isEmpty() && !update)
			callback.complete(STORED_INVITEINVS.get(0));
		else {
			final List<Player> cloneList = List.copyOf(Bukkit.getOnlinePlayers());
			final int playersSize = cloneList.size();
			final List<Integer> empties = Collections.unmodifiableList(iInvite.getEmpties());
			final int emptySlots = empties.size();
			AtomicInteger checkingArea = new AtomicInteger(playersSize - emptySlots);
			AtomicInteger pageNumber = new AtomicInteger(0);
			onPlayerRepeatInvite(pageNumber, cloneList, empties, checkingArea, emptySlots, callback);
		}
	}

	public static void updateMute(Player player) {
		onPlayerMute(player, new CompletableFuture<ConcurrentLinkedDeque<Inventory>>(), true);
	}

	public static void updateInvite() {
		onPlayerInvite(new CompletableFuture<Inventory>(), true);
	}

	public static void updateRequest() {
		onPlayerRequest(new CompletableFuture<Inventory>(), true);
	}

	private void createConfig() {
		file_ = Paths.get(getDataFolder() + "/chat.yml").toFile();
		file2_ = Paths.get(getDataFolder() + "/menu.yml").toFile();
		if (!file_.exists())
			saveResource("chat.yml", false);
		if (!file2_.exists())
			saveResource("menu.yml", false);
		configuration = YamlConfiguration.loadConfiguration(file_);
		configuration_menu = YamlConfiguration.loadConfiguration(file2_);
	}

	public static void saveChat() {
		try {
			configuration.save(file_);
			configuration_menu.save(file2_);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void loadChat() {
		try {
			configuration.load(file_);
			configuration_menu.load(file2_);
		} catch (IOException | InvalidConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}
