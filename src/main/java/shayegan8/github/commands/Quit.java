package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;

public class Quit extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.quit.usage" ,"&e/chatp reload"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.quit.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.quit.block", "&equit from group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            sender.sendMessage(getPermissionMSG());
            return;
        }
        if(args.length != 0) {
            sender.sendMessage(getUsage());
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();

        MDatabase.isPlayerInGroup(senderUUID).thenAccept((isInGroup) -> {
            if(!isInGroup)
                ColorUtils.C((String) ChatPlugin.configuration.get("chatp.quit.notgroup", "&cYou are not in any group"));
            else
                MDatabase.setPlayerGroup(senderUUID, "none");
        }).exceptionally((exp) -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.quit.error", "&cAn error occurred")));
            throw new IllegalStateException(exp);
        });
    }
}
