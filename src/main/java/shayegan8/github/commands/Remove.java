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
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.usage" ,"&e/chatp block &cplayerName"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.block", "&eblock player from group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            sender.sendMessage(getPermissionMSG());
            return;
        }
        if(args.length != 1) {
            sender.sendMessage(getUsage());
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.notfound", "&cCant find this player :(")));
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

        MDatabase.getPlayerGroup(senderUUID).thenCombine(MDatabase.getPlayerGroup(argUUID), String::equals).thenCompose((bothInSameGroup) -> {
            if(!bothInSameGroup) {
                sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.notfound", "&cYour group and the requested player's group its not the same")));
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.isPlayerInGroup(senderUUID).thenCombine(MDatabase.isPlayerInGroup(Bukkit.getPlayer(argUUID).getUniqueId()), (player1, player2) -> player1 && player2);
        }).thenCompose((bothAreInGroup) -> {
            if(!bothAreInGroup) {
                sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.both", "&cboth players are not in group")));
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.getPlayerTag(senderUUID);
        }).thenAccept((tag) -> {
            if (tag.equals("admin") || tag.equals("staff")) {
                MDatabase.setPlayerGroup(argUUID, "none");
            } else {
                sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.cant", "&cYou are not admin or staff")));
            }
        }).exceptionally((exp) -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.remove.error", "&cAn error occurred")));
            throw new IllegalStateException(exp);
        });
    }

}
