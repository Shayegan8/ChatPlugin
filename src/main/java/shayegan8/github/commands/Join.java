package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;

public class Join extends CommandManager {

    @Override
    public String getUsage() {
        return"&e/chatp join groupname";
    }

    @Override
    public String getPermission() {
        return "chatp.base.join";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.join.notPlayer", "&cYou should be a player"))));
            return;
        }
        if(args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.join.usage", getUsage()))));
            return;
        }
        UUID senderUUID = player.getUniqueId();
        MDatabase.isPlayerInvited(senderUUID).thenAccept(invited -> {
            if(!invited)
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.join.invitedFirst", "&eYou should be invited first"))));
            else {
                MDatabase.setPlayerGroup(senderUUID, args[0]);
                MDatabase.setPlayerInGroup(senderUUID, true);
                Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.join.invited", "&eYou are now in %chatp_group% group"))));
            }
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.join.error", "&cAn error occurred")));
            throw new RuntimeException(exp);
        });;
    }
}
