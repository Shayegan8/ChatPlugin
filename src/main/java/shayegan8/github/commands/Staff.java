package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;

public class Staff extends CommandManager {

    @Override
    public String getPermission() {
        return "chatp.base.staff";
    }

    @Override
    public String getUsage() {
        return "&e/chatp staff {playerName}";
    }

    private void console(CommandSender sender, String[] args) {
        if(args.length != 1) {
            ChatPlugin.sendBMSG(sender, "chatp.staff.usageConsole", getUsage());
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            ChatPlugin.sendBMSG(sender, "chatp.staff.cantFindConsole", "&cCant find this player");
            return;
        }
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
        ChatPlugin.sendBMSG(sender, "chatp.staff.promotedConsole", "&aPromoted");
        MDatabase.setPlayerTag(argUUID, "staff");
    }

    private void player(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if(args.length != 1) {
            ChatPlugin.sendBMSG(player, "chatp.staff.usage", getUsage());
            return;
        }
        if(Bukkit.getPlayer(args[0]) == null) {
            ChatPlugin.sendBMSG(sender, "chatp.staff.cantFind", "&cCant find this player");
            return;
        }
        if(args[0].equals(sender.getName())) {
            ChatPlugin.sendBMSG(sender, "chatp.staff.cantFuckYourself", "&cYou are admin ;|");
            return;
        }
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
        MDatabase.getPlayerTag(uuid).thenAccept(tag -> {
           if(!tag.equalsIgnoreCase("admin")) {
               ChatPlugin.sendBMSG(sender, "chatp.staff.cantFuckIt", "&cYou should be admin");
               return;
           }
           Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> MDatabase.setPlayerTag(argUUID, "staff"));
           ChatPlugin.sendCMSG(player, "chatp.staff.promoted", "&e%player_name% &aSuccessfully promoted");
        }).exceptionallyAsync(exp -> {
            throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
        });
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof Player)
            player(sender, args);
        else
            console(sender, args);
    }
}
