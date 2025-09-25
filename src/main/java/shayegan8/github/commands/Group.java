package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.database.MDatabase;

public class Group extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp group";
    }

    @Override
    public String getPermissionMSG() {
        return "You dont have a permission!";
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public String getPermission() {
        return "chatp.base.group";
    }

    @Override
    public String getDescription() {
        return "group command";
    }

    @Override
    public void execute(CommandSender sender, String[] args) { //chatp group create/delete {name}
        if (!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            sender.sendMessage(getPermissionMSG());
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(getUsage());
            return;
        }
        switch(args[0]) {
            case "create":
                MDatabase.createGroup(args[1]);
                break;
            case "delete":
                MDatabase.deleteGroup(args[1]);
                break;
            default:
                break;
        }
    }

}
