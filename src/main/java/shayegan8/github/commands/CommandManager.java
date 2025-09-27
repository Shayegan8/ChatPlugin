package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

public abstract class CommandManager {

    public String getUsage() {
        return null;
    }

    public abstract String getPermission();

    public abstract void execute(CommandSender sender, String[] args);

}
