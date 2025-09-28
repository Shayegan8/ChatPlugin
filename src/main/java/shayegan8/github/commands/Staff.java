package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Staff extends CommandManager {

    @Override
    public String getPermission() {
        return "chatp.base.staff";
    }

    @Override
    public String getUsage() {
        return "&e/chatp staff {playerName}";
    }

    private void console(CommandSender sender, String[] args) {
        if(args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.staff.usageConsole", getUsage()))));
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.staff.cantFindConsole", "&cCant find this player"))));
            return;
        }
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
        CompletableFuture.runAsync(() -> {
            MDatabase.setPlayerTag(argUUID, "staff");
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.staff.promotedConsole", "&aPromoted"))));
        }).exceptionallyAsync(exp -> {
            throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
        });
    }

    private void player(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if(args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.staff.usage", getUsage()))));
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.staff.cantFind", "&cCant find this player"))));
            return;
        }
        if(args[0].equals(sender.getName())) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.staff.cantFuckYourself", "&cYou are admin ;|"))));
            return;
        }
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
        MDatabase.getPlayerTag(uuid).thenAccept(tag -> {
           if(!tag.equalsIgnoreCase("admin"))
               return;
           MDatabase.setPlayerTag(argUUID, "staff");
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.staff.promoted", "&e%player_name% &aSuccessfully promoted"))));
        }).exceptionallyAsync(exp -> {
            throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
        });
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof Player)
            player(sender, args);
        else
            console(sender, args);
    }
}
