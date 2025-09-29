package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
                    ChatPlugin.sendBMSG(sender, "chatp.group.created", "&eGroup created");
                    break;
                case "delete":
                    MDatabase.deleteGroup(args[1]);
                    ChatPlugin.sendBMSG(sender, "chatp.group.deleted", "&eGroup deleted");
                    break;
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            CompletableFuture.supplyAsync(() -> ChatPlugin.configuration.getStringList("chatp.group.help"))
                    .thenAccept(ls -> ls.forEach(str -> Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> sender.sendMessage(ColorUtils.B(str)))))
                    .exceptionallyAsync(exp -> {
                throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
            });
        } else
            ChatPlugin.sendBMSG(sender, "chatp.group.usage", getUsage());
    }

    private void player(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (args.length == 2) {
            switch (args[0]) {
                case "create":
                    MDatabase.isPlayerInGroup(uuid).thenAccept((check) -> {
                        if(check) {
                            ChatPlugin.sendCMSG(player,"chatp.group.cantCreate", "&cYou are already in a group");
                            return;
                        }
                        Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
                            MDatabase.createGroup(args[1]);
                            MDatabase.setPlayerInGroup(uuid, true);
                            MDatabase.setPlayerTag(uuid, "admin");
                            MDatabase.setPlayerGroup(uuid, args[1]);
                        });
                        ChatPlugin.sendCMSG(player,"chatp.group.created", "&e%chatp_group% &ahas been created");
                    });
                    break;
                case "delete":
                    ChatPlugin.sendCMSG(player,"chatp.group.deleted", "&e%chatp_group% &chas been deleted");
                    MDatabase.isPlayerInGroup(uuid).thenCompose((check) -> {
                        if(!check) {
                            ChatPlugin.sendCMSG(player,"chatp.group.notIn", "&eYou are not in group");
                            return CompletableFuture.completedFuture(null);
                        }
                        return MDatabase.getPlayerTag(uuid);
                    }).thenAccept((tag) -> {
                        if(!tag.equalsIgnoreCase("admin")) {
                            ChatPlugin.sendCMSG(player,"chatp.group.admin", "&eYou are not admin");
                            return;
                        }
                        Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> {
                            MDatabase.deleteGroup(args[1]);
                            MDatabase.setPlayerInGroup(uuid, false);
                            MDatabase.setPlayerTag(uuid, "none");
                            MDatabase.setPlayerGroup(uuid, "none");
                        });
                    });
                    break;
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            CompletableFuture.supplyAsync(() -> ChatPlugin.configuration.getStringList("chatp.group.help")).thenAccept(ls -> {
                ls.forEach(str -> {
                    Bukkit.getScheduler().runTask(ChatPlugin.getInstance(), () -> sender.sendMessage(ColorUtils.C(player, str)));
                });
            }).exceptionallyAsync(exp -> {
                throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
            });
        } else
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.group.usage" , getUsage()))));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player))
            console(sender, args);
        else
            player(sender, args);
    }
}
