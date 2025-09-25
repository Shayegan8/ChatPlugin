package shayegan8.github.commands;

import org.bukkit.command.CommandSender;

public abstract class CommandManager {
    
    public abstract String getUsage();

    public abstract String getPermissionMSG();

    public abstract String getPermission();

    public abstract String getDescription();

    public abstract void execute(CommandSender sender, String[] args);

}
