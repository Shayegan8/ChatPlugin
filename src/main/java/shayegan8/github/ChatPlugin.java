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
import org.bukkit.permissions.PermissionDefault;
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
import shayegan8.github.gui.Gui;
import shayegan8.github.gui.IDelete;
import shayegan8.github.gui.IInvite;
import shayegan8.github.gui.IMenu;
import shayegan8.github.gui.IMute;
import shayegan8.github.gui.IRequest;
import shayegan8.github.gui.ItemSaver;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Plugin(name = "ChatPlugin", version = "1.0.0")
@Description("Simple chat plugin :O")
@Author("Shayegan8")
@Permission(name = "chatp.base.help", desc = "chatplugin help command", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.reload", desc = "chatplugin reload command", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.remove", desc = "Chatplugin remove permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.quit", desc = "Chatplugin quit permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.mute", desc = "Chatplugin mute permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.menu", desc = "Chatplugin menu permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.join", desc = "Chatplugin join permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.invite", desc = "Chatplugin invite permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.group", desc = "Chatplugin group permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.friends", desc = "Chatplugin friends permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.*", desc = "Chatplugin wildcard permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base", desc = "Chatplugin base permission", defaultValue = PermissionDefault.OP)
@Commands({ @Command(name = "chatp", desc = "chatplugin base command", permission = "chatp.base", usage = "/chatp") })
@ApiVersion(Target.v1_13)
@SoftDependency("PlaceholderAPI")
public final class ChatPlugin extends JavaPlugin {

	private static File file_;
	private static File file2_;
	public static FileConfiguration configuration;
	public static FileConfiguration configuration_menu;
	public final static Map<String, CommandManager> commands = Map.of("group", new Group(), "help", new Help(),
			"invite", new Invite(), "join", new Join(), "mute", new Mute(), "quit", new Quit(), "reload", new ReloadC(),
			"remove", new Remove(), "staff", new Staff(), "menu", new CMenu());
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
		iMute = new IMute();
		iInvite = new IInvite();
		iRequest = new IRequest();
		iDelete = new IDelete();
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

	public static ItemSaver getSkullOfOwner(Player player) {
		final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		final SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwnerProfile(player.getPlayerProfile());
		head.setItemMeta(meta);
		return new ItemSaver(head, player);
	}

	public static final Map<UUID, Inventory> STORED_MENUINVS = new ConcurrentHashMap<>();
	public static final Map<UUID, Inventory> STORED_DELETEINVS = new ConcurrentHashMap<>();

	public static void onPlayerMenu(Player player, Map<String, Entry> entries, CompletableFuture<Inventory> callback) {
		final Optional<Inventory> playerInv = Optional.ofNullable(STORED_MENUINVS.get(player.getUniqueId()));
		if (playerInv.isPresent()) {
			CompletableFuture.supplyAsync(() -> playerInv.get()).thenAccept(firstInventory -> Bukkit.getScheduler()
					.runTask(getInstance(), () -> player.openInventory(firstInventory)));
			return;
		}
		final Inventory inventory = Bukkit.createInventory(null, iMenu.getSize());
		normalFiller(player, entries, inventory);
		STORED_MENUINVS.put(player.getUniqueId(), inventory);
		callback.complete(inventory);
		callback.thenAccept(firstInventory -> Bukkit.getScheduler().runTask(getInstance(),
				() -> player.openInventory(firstInventory)));
	}

	private static void normalFiller(Player player, Map<String, Entry> entries, Inventory inv) {
		entries.entrySet().stream().forEach((entry) -> {
			final Entry value = entry.getValue();
			final ItemStack item = value.item();
			value.slots().forEach(slot -> {
				Bukkit.getScheduler().runTask(getInstance(), () -> {
					final ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(
							PlaceholderAPI.setPlaceholders(player, ColorUtils.C(player, value.displayName())));
					meta.setLore(value.lore().stream()
							.map(each -> PlaceholderAPI.setPlaceholders(player, ColorUtils.C(player, each)))
							.collect(Collectors.toList()));
					item.setItemMeta(meta);
					inv.setItem(slot, item);
				});
			});
		});
	}

	/**
	 * {pageNumber, items(the slot, and player name actually)} actually I can remove
	 * this <> but my IDE throws a warning
	 */
	public static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Inventory>> STORED_MUTEINVS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Inventory>> STORED_INVITEINVS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Inventory>> STORED_GROUPINVS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Inventory>> STORED_REQUESTINVS = new ConcurrentHashMap<>();
	
	private static void onPlayerRepeat(UUID senderUUID, int checkingArea, int emptySlotsSize, Stack<UUID> cloneStack,
			List<Integer> emptySlots, AtomicInteger pageNumber,
			ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Inventory>> collection, Gui gui,
			CompletableFuture<Inventory> callback) {
		final ConcurrentHashMap<Integer, Inventory> pages = new ConcurrentHashMap<>();
		final AtomicReference<Inventory> firstInventory = new AtomicReference<>();
		final Player player = Bukkit.getPlayer(senderUUID);
		if (checkingArea < emptySlotsSize) {
			final Inventory inventory = Bukkit.createInventory(null, gui.getSize(), ColorUtils.C(player, gui.getTitle()));
			normalFiller(Bukkit.getPlayer(senderUUID), gui.getEntries(), inventory);
			outer: for (UUID playerUUID : cloneStack.reversed())
				for (int emptySlot : emptySlots) {
					final Player pUUID = Bukkit.getPlayer(playerUUID);
					final ItemSaver playerHead = getSkullOfOwner(pUUID);
					final ItemStack item = playerHead.item();
					final ItemMeta meta = item.getItemMeta();
					final List<String> gLore = gui.getLore().stream().map(each -> ColorUtils.C(player, each)).collect(Collectors.toUnmodifiableList());
					Bukkit.getScheduler().runTask(getInstance(), () -> {
						meta.setLore(gLore);
						meta.setDisplayName(ColorUtils.C(player, "&7" + pUUID.getDisplayName()));
						item.setItemMeta(meta);
						inventory.setItem(emptySlot, item);
					});
					continue outer;
				}
			Bukkit.getScheduler().runTask(getInstance(), () -> {
				pages.put(pageNumber.get(), inventory);
				collection.put(senderUUID, pages);
				if (pageNumber.get() == 0) {
					firstInventory.lazySet(inventory);
					callback.complete(firstInventory.get());
				}
			});

		} else { // checkingArea >= emptySlotsSize
			final Inventory inventory = Bukkit.createInventory(null, gui.getSize(), ColorUtils.C(player, gui.getTitle()));
			normalFiller(player, gui.getEntries(), inventory);
			outer: for (UUID playerUUID : cloneStack)
				for (int emptySlot : emptySlots) {
					final Player pUUID = Bukkit.getPlayer(playerUUID);
					final ItemSaver playerHead = getSkullOfOwner(pUUID);
					final ItemStack item = playerHead.item();
					final ItemMeta meta = item.getItemMeta();
					final List<String> gLore = gui.getLore().stream().map(each -> ColorUtils.C(player, each)).collect(Collectors.toUnmodifiableList());
					Bukkit.getScheduler().runTask(getInstance(), () -> {
						meta.setLore(gLore);
						meta.setDisplayName(ColorUtils.C(player, "&7" + pUUID.getDisplayName()));
						item.setItemMeta(meta);
						inventory.setItem(emptySlot, item);
					});
					continue outer;
				}
			for (int remove = 1; remove < emptySlotsSize; remove++)
				cloneStack.pop();
			checkingArea = cloneStack.size() - emptySlotsSize;
			Bukkit.getScheduler().runTask(getInstance(), () -> {
				pages.put(pageNumber.getAndIncrement(), inventory);
				if (pageNumber.get() == 0)
					firstInventory.lazySet(inventory);
			});
			onPlayerRepeat(senderUUID, checkingArea, emptySlotsSize, cloneStack, emptySlots, pageNumber, collection,
					gui, callback);
		}
	}

	public static void onPlayerRequest(Player player, CompletableFuture<Inventory> callback) {
		Optional<ConcurrentHashMap<Integer, Inventory>> map = Optional
				.ofNullable(STORED_REQUESTINVS.get(player.getUniqueId()));
		if (map.isPresent()) {
			CompletableFuture.supplyAsync(() -> map.get()).thenAccept(inventory -> Bukkit.getScheduler()
					.runTask(getInstance(), () -> player.openInventory(inventory.get(0))));
			return;
		}
		List<Integer> emptySlots = iInvite.getEmpties();
		int emptySlotsSize = emptySlots.size();
		AtomicInteger pageNumber = new AtomicInteger(0);
		Stack<UUID> cloneStack = new Stack<>();
		Bukkit.getOnlinePlayers().stream().forEach(each -> cloneStack.push(each.getUniqueId()));
		int playersGroupsSize = cloneStack.size();
		int checkingArea = playersGroupsSize - emptySlotsSize;
		CompletableFuture.runAsync(() -> {
			onPlayerRepeat(player.getUniqueId(), checkingArea, emptySlotsSize, cloneStack, emptySlots, pageNumber,
					STORED_REQUESTINVS, iRequest, callback);
		}, EVIRTUAL);
	}

	public static void onPlayerDelete(Player player, CompletableFuture<Inventory> callback) {
		final Optional<Inventory> playerInv = Optional.ofNullable(STORED_DELETEINVS.get(player.getUniqueId()));
		if (playerInv.isPresent()) {
			CompletableFuture.supplyAsync(() -> playerInv.get()).thenAccept(firstInventory -> Bukkit.getScheduler()
					.runTask(getInstance(), () -> player.openInventory(firstInventory)));
			return;
		}
		final Inventory inventory = Bukkit.createInventory(null, iDelete.getSize(),
				ColorUtils.C(player, iDelete.getTitle()));
		CompletableFuture.runAsync(() -> {
			normalFiller(player, iDelete.getEntries(), inventory);
			STORED_DELETEINVS.put(player.getUniqueId(), inventory);
			Bukkit.getScheduler().runTask(getInstance(), () -> {
				callback.complete(inventory);
				callback.thenAccept(firstInventory -> Bukkit.getScheduler().runTask(getInstance(),
						() -> player.openInventory(firstInventory)));
			});
		}, EVIRTUAL);

	}

	public static void onPlayerInvite(Player player, CompletableFuture<Inventory> callback) {
		Optional<ConcurrentHashMap<Integer, Inventory>> map = Optional
				.ofNullable(STORED_INVITEINVS.get(player.getUniqueId()));
		if (map.isPresent()) {
			CompletableFuture.supplyAsync(() -> map.get()).thenAccept(inventory -> Bukkit.getScheduler()
					.runTask(getInstance(), () -> player.openInventory(inventory.get(0))));
			return;
		}
		List<Integer> emptySlots = iInvite.getEmpties();
		int emptySlotsSize = emptySlots.size();
		AtomicInteger pageNumber = new AtomicInteger(0);
		Stack<UUID> cloneStack = new Stack<>();
		Bukkit.getOnlinePlayers().stream().forEach(each -> cloneStack.push(each.getUniqueId()));
		int playersGroupsSize = cloneStack.size();
		int checkingArea = playersGroupsSize - emptySlotsSize;
		onPlayerRepeat(player.getUniqueId(), checkingArea, emptySlotsSize, cloneStack, emptySlots, pageNumber,
				STORED_INVITEINVS, iInvite, callback);
	}

	public static void onPlayerMute(Player player, CompletableFuture<Inventory> callback) {
		Optional<ConcurrentHashMap<Integer, Inventory>> map = Optional
				.ofNullable(STORED_MUTEINVS.get(player.getUniqueId()));
		if (map.isPresent()) {
			CompletableFuture.supplyAsync(() -> map.get()).thenAccept(inventory -> Bukkit.getScheduler()
					.runTask(getInstance(), () -> player.openInventory(inventory.get(0))));
			return;
		}
		List<Integer> emptySlots = iMute.getEmpties();
		int emptySlotsSize = emptySlots.size();
		AtomicInteger pageNumber = new AtomicInteger(0);
		MDatabase.getPlayerGroup(player.getUniqueId())
				.thenCompose(playerGroup -> MDatabase.getPlayersByGroup(playerGroup)).thenAccept(playerUUIDStack -> {
					Stack<UUID> cloneStack = playerUUIDStack;
					int playersGroupsSize = cloneStack.size();
					int checkingArea = playersGroupsSize - emptySlotsSize;
					onPlayerRepeat(player.getUniqueId(), checkingArea, emptySlotsSize, cloneStack, emptySlots,
							pageNumber, STORED_MUTEINVS, iMute, callback);

				});
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