package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Remove extends CommandManager {

    @Override
    public String getUsage() {
        return "&e/chatp block &cplayerName";
    }

    @Override
    public String getPermission() {
        return "chatp.base.remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.remove.notPlayer", "&cYou should be a player"))));
            return;
        }
        if(args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getUsage()));
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.remove.notFound", "&cCant find this player :("))));
            return;
        }
        UUID senderUUID = player.getUniqueId();
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

        MDatabase.getPlayerGroup(senderUUID).thenCombine(MDatabase.getPlayerGroup(argUUID), String::equals).thenCompose(bothInSameGroup -> {
            if(!bothInSameGroup) {
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.remove.notSame", "&cYour group and the requested player's group its not the same"))));
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.isPlayerInGroup(senderUUID).thenCombine(MDatabase.isPlayerInGroup(Bukkit.getPlayer(argUUID).getUniqueId()), (player1, player2) -> player1 && player2);
        }).thenCompose(bothAreInGroup -> {
            if(!bothAreInGroup) {
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.remove.both", "&cboth players are not in group"))));
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.getPlayerTag(senderUUID);
        }).thenAccept(tag -> {
            if (tag.equals("admin") || tag.equals("staff")) {
                MDatabase.setPlayerGroup(argUUID, "none");
                MDatabase.setPlayerTag(argUUID, "none");
                MDatabase.setPlayerInGroup(argUUID, false);
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.remove.removed", "%player_name% &asuccessfully removed"))));
            } else {
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.remove.cant", "&cYou are not admin or staff"))));
            }
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.remove.error", "&cAn error occurred")));
            throw new RuntimeException(exp);
        });
    }

}
