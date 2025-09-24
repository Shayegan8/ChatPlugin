package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.database.MDatabase;

import java.util.concurrent.CompletableFuture;

public class Remove extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp block playerName";
    }

    @Override
    public String getPermissionMSG() {
        return "You dont have a permission!";
    }

    @Override
    public String getName() {
        return "block";
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return "block player from group";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof Player && sender.hasPermission(getPermission())) {
            if(args.length == 2) {
                String senderName = sender.getName();
                CompletableFuture<String> senderGroup = MDatabase.getPlayerGroup(senderName);
                CompletableFuture<String> playerGroup = MDatabase.getPlayerGroup(args[1]);
                CompletableFuture<Boolean> comparingGroups = senderGroup.thenCombine(playerGroup, String::equals);
                CompletableFuture<Boolean> senderInGroup = MDatabase.isPlayerInGroup(senderName);
                CompletableFuture<Boolean> playerInGroup = MDatabase.isPlayerInGroup(args[1]);
                CompletableFuture<Boolean> bothInGroup = senderInGroup.thenCombine(playerInGroup, (player1, player2) -> player1.equals(true) && player2.equals(true));
                bothInGroup.thenAccept((both) -> {
                    if(both) {
                        comparingGroups.thenAccept((group) -> {
                            if(group) {
                                CompletableFuture<String> senderTag = MDatabase.getPlayerTag(sender.getName());
                                senderTag.thenAccept((tag) -> {
                                    if(tag.equalsIgnoreCase("admin")) {

                                    }
                                });
                            } else {
                                sender.sendMessage("Your group and the requested player's group its not the same");
                            }
                        });
                    }
                });
            } else {
                sender.sendMessage(getUsage());
            }
        } else {
            sender.sendMessage(getPermissionMSG());
        }
    }
}
