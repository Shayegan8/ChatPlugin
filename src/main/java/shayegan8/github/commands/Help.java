package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class Help extends CommandManager {

    @Override
    public String getPermission() {
        return "chatp.base.help";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CompletableFuture.supplyAsync(() -> ChatPlugin.configuration.getStringList("chatp.help.list")).thenAccept(ls -> {
            ls.forEach(str -> {
                if(sender instanceof Player player)
                    Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> sender.sendMessage(ColorUtils.C(player, str)));
                else
                    Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> sender.sendMessage(ColorUtils.B(str)));
            });
        }).exceptionallyAsync(exp -> {
            throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
        });
    }
}
