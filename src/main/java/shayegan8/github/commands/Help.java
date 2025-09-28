package shayegan8.github.commands;

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
        CompletableFuture.runAsync(() -> {
            ChatPlugin.configuration.getStringList("chatp.help.list").forEach(str -> {
                if(sender instanceof Player player)
                    sender.sendMessage(ColorUtils.C(player, str));
                else
                    sender.sendMessage(ColorUtils.B(str));
            });
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.help.error", "&cAn error occurred")));
            throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
        });
    }
}
