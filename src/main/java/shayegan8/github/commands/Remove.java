package shayegan8.github.commands;

import org.bukkit.Bukkit;
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
        if(!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            sender.sendMessage(getPermissionMSG());
            return;
        }
        if(args.length != 2) {
            sender.sendMessage(getUsage());
            return;
        }
        if (Bukkit.getPlayer(args[1]) == null) {
            sender.sendMessage("cant find that player");
            return;
        }
        MDatabase.getPlayerGroup(sender.getName()).thenCombine(MDatabase.getPlayerGroup(args[1]), String::equals).thenCompose((bothInSameGroup) -> {
            if(!bothInSameGroup) {
                sender.sendMessage("Your group and the requested player's group its not the same");
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.isPlayerInGroup(sender.getName()).thenCombine(MDatabase.isPlayerInGroup(args[1]), (player1, player2) -> player1 && player2);
        }).thenCompose((bothAreInGroup) -> {
            if(!bothAreInGroup) {
                sender.sendMessage("both players are not in group");
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.getPlayerTag(sender.getName());
        }).thenAccept((tag) -> {
            if (tag.equalsIgnoreCase("admin") || tag.equalsIgnoreCase("staff")) {
                MDatabase.setPlayerGroup(args[1], "none");
            } else {
                sender.sendMessage("You are not admin or staff");
            }
        }).exceptionally((exp) -> {
            sender.sendMessage("An error occurred");
            throw new IllegalStateException(exp.getMessage());
        });;
    }

}
