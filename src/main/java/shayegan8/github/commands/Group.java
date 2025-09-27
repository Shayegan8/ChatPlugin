package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;

public class Group extends CommandManager {

    @Override
    public String getUsage() {
        return "&e/chatp group help,create,delete";
    }

    @Override
    public String getPermission() {
        return "chatp.base.group";
    }

    private void console(CommandSender sender, String[] args) {
        if (args.length == 2) {
            switch (args[0]) {
                case "create":
                    MDatabase.createGroup(args[1]);
                    Thread.ofVirtual().start(() -> ChatPlugin.getPlugin(ChatPlugin.class).getLogger().info(ColorUtils.B(ChatPlugin.configuration.getString("chatp.group.createdConsole", "&eGroup created"))));
                    break;
                case "delete":
                    MDatabase.deleteGroup(args[1]);
                    Thread.ofVirtual().start(() -> ChatPlugin.getPlugin(ChatPlugin.class).getLogger().info(ColorUtils.B(ChatPlugin.configuration.getString("chatp.group.deletedConsole", "&eGroup deleted"))));
                    break;
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            Thread.ofVirtual().start(() -> {
                ChatPlugin.configuration.getStringList("chatp.group.helpConsole").forEach(each -> {
                    sender.sendMessage(ColorUtils.B(each));
                });
            });
        } else
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.group.usageConsole" , getUsage()))));
    }

    private void player(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (args.length == 2) {
            switch (args[0]) {
                case "create":
                    MDatabase.createGroup(args[1]);
                    MDatabase.setPlayerInGroup(uuid, true);
                    MDatabase.setPlayerTag(uuid, "admin");
                    MDatabase.setPlayerGroup(uuid, args[1]);
                    Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.group.created", "&e%chatp_group% &ahas been created"))));
                    break;
                case "delete":
                    Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.group.deleted", "&e%chatp_group% &chas been deleted"))));
                    MDatabase.deleteGroup(args[1]);
                    MDatabase.setPlayerInGroup(uuid, false);
                    MDatabase.setPlayerTag(uuid, "none");
                    MDatabase.setPlayerGroup(uuid, "none");
                    break;
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            Thread.ofVirtual().start(() -> {
                ChatPlugin.configuration.getStringList("chatp.group.help").forEach(each -> {
                    sender.sendMessage(ColorUtils.C(player, each));
                });
            });
        } else
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.group.usage" , getUsage()))));
    }

    @Override
    public void execute(CommandSender sender, String[] args) { //chatp group create/delete {name} or /ch
        if(!(sender instanceof Player))
            console(sender, args);
        else
            player(sender, args);
    }

}
