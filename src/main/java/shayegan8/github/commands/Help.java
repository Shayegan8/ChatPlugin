package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.concurrent.CompletableFuture;

public class Help extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.help.usage" ,"&e/chatp help"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.help.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.help";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.help.desc", "&ejoin to a group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CompletableFuture.runAsync(() -> {
            ChatPlugin.configuration.getStringList("player.help").forEach(str -> {
                sender.sendMessage(ColorUtils.C(str));
            });
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.help.error", "&cAn error occurred")));
            throw new RuntimeException(exp);
        });
    }
}
