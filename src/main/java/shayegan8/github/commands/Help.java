package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.concurrent.CompletableFuture;

public class Help extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp help";
    }

    @Override
    public String getPermissionMSG() {
        return (String) ChatPlugin.configuration.get("chatp.base.help", "&cYou do not have permission!");
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "chatp.base.help";
    }

    @Override
    public String getDescription() {
        return "chatplugin help command";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CompletableFuture.runAsync(() -> {
            ChatPlugin.configuration.getStringList("player.help").forEach((str) -> {
                sender.sendMessage(ColorUtils.C(str));
            });
        }).exceptionally((exp) -> {
            sender.sendMessage("An error occurred");
            throw new IllegalStateException(exp.getMessage());
        });;
    }
}
