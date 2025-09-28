package shayegan8.github.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;

public class Quit extends CommandManager {

    @Override
    public String getUsage() {
        return "&e/chatp reload";
    }

    @Override
    public String getPermission() {
        return "chatp.base.quit";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.quit.notPlayer", "&cCant find this player"))));
            return;
        }
        if(args.length != 0) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.quit.usage", getUsage()))));
            return;
        }
        UUID senderUUID = player.getUniqueId();

        MDatabase.isPlayerInGroup(senderUUID).thenAccept(isInGroup -> {
            if(!isInGroup)
                Thread.ofVirtual().start(() -> ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.quit.notGroup", "&cYou are not in any group")));
            else {
                MDatabase.setPlayerTag(senderUUID, "none");
                MDatabase.setPlayerGroup(senderUUID, "none");
                MDatabase.setPlayerInGroup(senderUUID, false);
                Thread.ofVirtual().start(() -> ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.quit.quit", "&eYou are no longer in this group")));
            }
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.quit.error", "&cAn error occurred")));
            throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
        });
    }
}
