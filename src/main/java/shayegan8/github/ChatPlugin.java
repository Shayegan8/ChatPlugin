package shayegan8.github;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import shayegan8.github.commands.*;
import shayegan8.github.database.MDatabase;
import shayegan8.github.events.Grouping;
import shayegan8.github.expansions.PPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;

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
@Commands(
    {
        @Command(name = "chatp", desc = "chatplugin base command", permission = "chatp.base", usage = "/chatp"),
        @Command(name = "chatp help", desc = "chatplugin help command", permission = "chatp.base.help", usage = "/chatp help"),
        @Command(name = "chatp reload", desc = "chatplugin reload command", permission = "chatp.base.reload", usage = "/chatp reload"),
        @Command(name = "chatp remove", desc = "chatplugin help command", permission = "chatp.base.remove", usage = "/chatp remove playername"),
        @Command(name = "chatp invite", desc = "chatplugin invite command", permission = "chatp.base.invite", usage = "/chatp invite playername"),
        @Command(name = "chatp quit", desc = "chatplugin quit command", permission = "chatp.base.quit", usage = "/chatp quit"),
        @Command(name = "chatp mute", desc = "chatplugin mute command", permission = "chatp.base.mute", usage = "/chatp mute playername"),
        @Command(name = "chatp join", desc = "chatplugin join command", permission = "chatp.base.join", usage = "/chatp join group"),
        @Command(name = "chatp group", desc = "chatplugin group command", permission = "chatp.base.group", usage = "/chatp group"),
        @Command(name = "chatp friends", desc = "chatplugin friends command", permission = "chatp.base.friends", usage = "/chatp friends")
    }
)
@SoftDependency("PlaceholderAPI")

public final class ChatPlugin extends JavaPlugin {

    private static File file_;
    private static File file2_;
    public static FileConfiguration configuration;
    public static FileConfiguration configuration_menu;
    public static Map<String, CommandManager> commands = Map.of
            ("group", new Group(), "help", new Help(), "invite", new Invite(), "join", new Join(), "mute", new Mute(), "quit", new Quit(), "reload", new ReloadC(), "remove", new Remove());
    public final static Map<String, Integer> tags = Map.of("none", 1, "staff", 2, "admin", 3);
    public static MDatabase mDB;
    private static final String GREEN = "\u001b[32m";
    private static final String RED = "\u001b[31m";
    private static final String REFRESH = "\u001b[0m";

    @Override
    public void onEnable() {
        getLogger().info("Configuration...");
        createConfig();
        getLogger().info("Registering commands...");
        this.getCommand("chatp").setExecutor(new BaseCommand());
        this.getCommand("chatp").setTabCompleter(new BaseCommand());
        getLogger().info(ColorUtils.C("Establishing connection to database..."));
        mDB = new MDatabase();
        getLogger().info("Registering events...");
        getServer().getPluginManager().registerEvents(new Grouping(), this);
        getLogger().info("Registering placeholders (PlaceholderAPI)");
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PPlayer(this).register();
            getLogger().info(GREEN + "ChatPlugin enabled" + REFRESH);
        } else {
            getLogger().info(RED + "Couldn't find PlaceholderAPI, disabling plugin..." + REFRESH);
            getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if(mDB.getConnection() != null)
                mDB.getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        getLogger().info(ColorUtils.C(RED + "ChatPlugin disabled" + REFRESH));
    }

    private void createConfig() {
        file_ = Paths.get(getDataFolder() + "/chat.yml").toFile();
        file2_ = Paths.get(getDataFolder() + "/menu.yml").toFile();
        if(!file_.exists())
            saveResource("chat.yml", false);
        if(!file2_.exists())
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