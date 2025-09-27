package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.concurrent.CompletableFuture;

public class ReloadC extends CommandManager {

    @Override
    public String getPermission() {
        return "chatp.base.reload";
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        CompletableFuture.runAsync(ChatPlugin::loadChat).exceptionallyAsync(exp -> {
            if(sender instanceof Player player)
                sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.reload.error", "&cAn error occurred")));
            else {
                sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.reload.errorConsole", "&cAn error occurred")));
            }
            throw new RuntimeException(exp);
        });
        sender.sendMessage("Plugin configuration reloaded");
    }
}
