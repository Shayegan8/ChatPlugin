package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.database.MDatabase;

import java.util.UUID;

public class Join extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp join groupName";
    }

    @Override
    public String getPermissionMSG() {
        return "You do not have a permission!";
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getPermission() {
        return "chatp.base.join";
    }

    @Override
    public String getDescription() {
        return "it joins you to your group";
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
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        MDatabase.isPlayerInvited(senderUUID).thenAccept((invited) -> {
            if(!invited)
                sender.sendMessage("You should be invited first");
            else
                MDatabase.setPlayerGroup(senderUUID, args[0]);
        });
    }
}
