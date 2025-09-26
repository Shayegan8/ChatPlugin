package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Join extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.join.usage" ,"&e/chatp join groupname"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.join.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.join.block", "&ejoin to a group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getPermissionMSG()));
            return;
        }
        if(args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getUsage()));
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        MDatabase.isPlayerInvited(senderUUID).thenAccept(invited -> {
            if(!invited)
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.join.invitedfirst", "You should be invited first"))));
            else
                MDatabase.setPlayerGroup(senderUUID, args[0]);
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.join.error", "&cAn error occurred")));
            throw new RuntimeException(exp);
        });;
    }
}
