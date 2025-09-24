package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

public class Friends extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp friends";
    }

    @Override
    public String getPermissionMSG() {
        return "You dont have a permission!";
    }

    @Override
    public String getName() {
        return "friends";
    }

    @Override
    public String getPermission() {
        return "chatp.base.friends";
    }

    @Override
    public String getDescription() {
        return "friends command";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
