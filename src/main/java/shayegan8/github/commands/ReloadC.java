package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.concurrent.CompletableFuture;

public class ReloadC extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.reload.usage" ,"&e/chatp reload"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.reload.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.reload.block", "&ereload config"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CompletableFuture.runAsync(ChatPlugin::loadChat).exceptionally((exp) -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.reload.error", "&cAn error occurred")));
            throw new IllegalStateException(exp.getMessage());
        });
        sender.sendMessage("Plugin configuration reloaded");
    }
}
