package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

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
    public void execute(CommandSender sender, String[] args) {

    }

}
