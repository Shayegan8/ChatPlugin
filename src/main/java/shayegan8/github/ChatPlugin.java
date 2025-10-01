package shayegan8.github;

import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import shayegan8.github.commands.*;
import shayegan8.github.database.MDatabase;
import shayegan8.github.events.Grouping;
import shayegan8.github.expansions.Placeholders;
import shayegan8.github.gui.IMenu;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(name = "ChatPlugin", version = "1.0.0")
@Description("Simple chat plugin :O")
@Author("Shayegan8")
@Permission(name = "chatp.base.help", desc = "chatplugin help command", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.reload", desc = "chatplugin reload command", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.remove", desc = "Chatplugin remove permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.quit", desc = "Chatplugin quit permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.mute", desc = "Chatplugin mute permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.join", desc = "Chatplugin join permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.invite", desc = "Chatplugin invite permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.group", desc = "Chatplugin group permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.friends", desc = "Chatplugin friends permission", defaultValue = PermissionDefault.OP)
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
	public static Map<String, CommandManager> commands = Map.of("group", new Group(), "help", new Help(), "invite",
			new Invite(), "join", new Join(), "mute", new Mute(), "quit", new Quit(), "reload", new ReloadC(), "remove",
			new Remove(), "staff", new Staff(), "menu", new CMenu());
	public final static Map<String, Integer> tags = Map.of("none", 1, "staff", 2, "admin", 3);
	public static Map<String, String> entries = new ConcurrentHashMap<String, String>();
	public static MDatabase mDB;
	private static final String GREEN = "\u001b[32m";
	private static final String RED = "\u001b[31m";
	private static final String REFRESH = "\u001b[0m";
	private static final String URL = "";
	private FileOutputStream fout;
	public static IMenu iMenu;
	private static ChatPlugin plugin;

	public static void sendBMSG(CommandSender sender, String path, String msg) {
		Bukkit.getScheduler().runTask(getInstance(), () -> sender.sendMessage(configuration.getString(path, msg)));
	}

	public static void sendCMSG(Player player, String path, String msg) {
		Bukkit.getScheduler().runTask(getInstance(), () -> player.sendMessage(PlaceholderAPI.setPlaceholders(player, configuration.getString(path, msg))));
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
		getLogger().info("Registering commands...");
		this.getCommand("chatp").setExecutor(new BaseCommand(new CooldownManager()));
		this.getCommand("chatp").setTabCompleter(new BaseCommand(new CooldownManager()));
		getLogger().info("Establishing connection to database...");
		mDB = new MDatabase(configuration.getString("dbName", "sqlite"));
		getLogger().info("Registering guis...");
		iMenu = new IMenu();
		getLogger().info("Registering events...");
		getServer().getPluginManager().registerEvents(new Grouping(), this);
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
		getServer().getPluginManager().registerEvents(iMenu, this);
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
		getLogger().info(RED + "ChatPlugin disabled" + REFRESH);
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