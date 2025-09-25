package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Mute extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp mute";
    }

    @Override
    public String getPermissionMSG() {
        return "You dont have a permission!";
    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getPermission() {
        return "chatp.base.mute";
    }

    @Override
    public String getDescription() {
        return "mute from a group";
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
            sender.sendMessage("Cant find this player");
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

        MDatabase.isPlayerInGroup(senderUUID).thenCombine(MDatabase.isPlayerInGroup(argUUID), (player1, player2) -> player1 && player2).thenCompose((inGroup) -> {
           if(!inGroup) {
               sender.sendMessage("You or that player should be in a group");
               return CompletableFuture.completedFuture(null);
           }
           return MDatabase.getPlayerTag(senderUUID).thenCombine(MDatabase.getPlayerTag(argUUID), (player1, player2) -> ChatPlugin.tags.get(player1) > ChatPlugin.tags.get(player2));
        }).thenAccept((obj) -> {
            if(!obj)
                sender.sendMessage("You cant mute this player");
            else
                MDatabase.setPlayerMuted(argUUID, true);
        });

    }

}
