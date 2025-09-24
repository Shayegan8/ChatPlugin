package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import shayegan8.github.ChatPlugin;

import java.util.concurrent.CompletableFuture;

public class ReloadC extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp reload";
    }

    @Override
    public String getPermissionMSG() {
        return "&cYou do not have permission!";
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "chatp.base.reload";
    }

    @Override
    public String getDescription() {
        return "chatplugin reload command";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CompletableFuture.runAsync(ChatPlugin::loadChat);
        sender.sendMessage("Plugin reloaded");
    }
}
