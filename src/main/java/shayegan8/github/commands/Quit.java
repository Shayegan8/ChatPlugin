package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

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

    }
}
