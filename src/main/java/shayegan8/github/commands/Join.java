package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

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

    }
}
