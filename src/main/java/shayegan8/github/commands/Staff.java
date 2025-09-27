package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

public class Staff extends CommandManager {

    @Override
    public String getPermission() {
        return "chatp.base.staff";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
