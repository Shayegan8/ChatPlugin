package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

public class Group extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.group.usage" ,"&e/chatp group help,create,delete"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.group.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.group.block", "&ecreate a group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) { //chatp group create/delete {name}
        if (!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getPermissionMSG()));
            return;
        }
        if (args.length != 2) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getUsage()));
            return;
        }
        switch(args[0]) {
            case "create":
                MDatabase.createGroup(args[1]);
                break;
            case "delete":
                MDatabase.deleteGroup(args[1]);
                break;
            case "help":
                sender.sendMessage(getUsage());
                break;
            default:
                break;
        }
    }

}
