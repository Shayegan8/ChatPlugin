package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Mute extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.mute.usage" ,"&e/chatp mute playername"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.mute.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.mute";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.mute.desc", "&emute players in group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getPermissionMSG()));
            return;
        }
        if(args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getUsage()));
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            Thread.ofVirtual().start(() -> sender.sendMessage("chatp.mute.cantPlayer", "&cCant find this player"));
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

        MDatabase.isPlayerInGroup(senderUUID).thenCombine(MDatabase.isPlayerInGroup(argUUID), (player1, player2) -> player1 && player2).thenCompose(inGroup -> {
           if(!inGroup) {
               Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.mute.notSame", "&cYou or that player should be in a group"))));
               return CompletableFuture.completedFuture(null);
           }
           return MDatabase.getPlayerTag(senderUUID).thenCombine(MDatabase.getPlayerTag(argUUID), (player1, player2) -> ChatPlugin.tags.get(player1) > ChatPlugin.tags.get(player2));
        }).thenAccept(obj -> {
            if(!obj)
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.mute.cantMute","&cYou cant mute this player"))));
            else
                MDatabase.setPlayerMuted(argUUID, true);
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.mute.error", "&cAn error occurred")));
            throw new RuntimeException(exp);
        });

    }

}
