package shayegan8.github.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class BaseCommand implements CommandExecutor, TabExecutor {

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String firstArgument = args[0];
        Stream.of("remove", "reload", "quit", "mute", "join", "invite", "help", "group", "friends").filter((x) -> x.startsWith(firstArgument)).forEach(completions::add);
        Collections.sort(completions);
        return completions;
    }
}
