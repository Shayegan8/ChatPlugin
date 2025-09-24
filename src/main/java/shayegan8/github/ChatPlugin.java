package shayegan8.github;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import shayegan8.github.commands.*;
import shayegan8.github.database.MDatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

@Plugin(name = "ChatPlugin", version = "1.0.0")
@Description("Simple chat plugin :O")
@Author("Shayegan8")
@Permission(name = "chatp.base.help", desc = "chatplugin help command", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base.reload", desc = "chatplugin reload command", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.*", desc = "Chatplugin wildcard permission", defaultValue = PermissionDefault.OP)
@Permission(name = "chatp.base", desc = "Chatplugin base permission", defaultValue = PermissionDefault.OP)
@Commands(
    {
        @Command(name = "chatp", desc = "chatplugin base command", permission = "chatp.base", usage = "/chatp"),
        @Command(name = "chatp help", desc = "chatplugin help command", permission = "chatp.base.help", usage = "/chatp help"),
        @Command(name = "chatp reload", desc = "chatplugin help command", permission = "chatp.base.reload", usage = "/chatp reload"),
        @Command(name = "chatp invite", desc = "chatplugin help command", permission = "chatp.base.reload", usage = "/chatp invite playername")
    }
)

public final class ChatPlugin extends JavaPlugin {

    public static File file_;

    public static FileConfiguration configuration;

    public static Map<String, CommandManager> commands = Map.of
            ("help", new Help(),
                    "reload", new ReloadC(),
                    "invite", new Invite()
            );

    public static MDatabase mDB;

    @Override
    public void onEnable() {
        getLogger().info(ColorUtils.C("Configuration..."));
        createConfig();
        getLogger().info(ColorUtils.C("Registering commands..."));
        this.getCommand("chatp").setExecutor(new BaseCommand());
        getLogger().info(ColorUtils.C("Establishing connection to database"));

        getLogger().info(ColorUtils.C("ChatPlugin enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ColorUtils.C("&cChatPlugin disabled"));
    }

    private void createConfig() {
        file_ = Paths.get(getDataFolder() + "/chat.yml").toFile();
        if(!file_.exists())
            saveResource("chat.yml", false);
        configuration = YamlConfiguration.loadConfiguration(file_);
    }

    public static void saveChat() {
        try {
            configuration.save(file_);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadChat() {
        try {
            configuration.load(file_);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
