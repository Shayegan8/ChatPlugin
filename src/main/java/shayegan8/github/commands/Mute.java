package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

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

    }

}
