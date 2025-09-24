package shayegan8.github.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.Arrays;

public class BaseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration
                    .get("chatp.argszero", "&c/chatp help to get instruction for all commands")));
            return true;
        }
        CommandManager cmd_ = ChatPlugin.commands.get(args[0].toLowerCase());
        if(cmd_ == null) {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration
                    .get("chatp.unknown", "&cUnknown command")));
            return true;
        }

        if(!sender.hasPermission(cmd_.getPermission())) {
            sender.sendMessage(ColorUtils.C(cmd_.getPermissionMSG()));
            return true;
        }

        cmd_.execute(sender, Arrays.copyOfRange(args, 1, args.length));

        return true;
    }
}
