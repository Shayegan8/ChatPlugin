package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Quit extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp quit";
    }

    @Override
    public String getPermissionMSG() {
        return "You dont have a permission!";
    }

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getPermission() {
        return "chatp.base.quit";
    }

    @Override
    public String getDescription() {
        return "quit from a group";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            sender.sendMessage(getPermissionMSG());
            return;
        }
        if(args.length != 0) {
            sender.sendMessage(getUsage());
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();

        MDatabase.isPlayerInGroup(senderUUID).thenAccept((isInGroup) -> {
            if(!isInGroup)
                sender.sendMessage("You are not in any group");
            else
                MDatabase.setPlayerGroup(senderUUID, "none");
        });
    }
}
