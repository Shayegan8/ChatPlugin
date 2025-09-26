package shayegan8.github.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Invite extends CommandManager {

    @Override
    public String getUsage() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.invite.usage" ,"&e/chatp invite playername"));
    }

    @Override
    public String getPermissionMSG() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.invite.permissionMSG", "&cYou dont have a permission!"));
    }

    @Override
    public String getPermission() {
        return "chatp.base.block";
    }

    @Override
    public String getDescription() {
        return ColorUtils.C((String) ChatPlugin.configuration.get("chatp.invite.block", "&einvite player to your group"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getPermissionMSG()));
            return;
        }
        if (args.length != 1) {
            Thread.ofVirtual().start(() -> sender.sendMessage(getUsage()));
            return;
        }
        if(sender.getName().equalsIgnoreCase(args[0])) {
            Thread.ofVirtual().start(() -> sender.sendMessage("chatp.invite.cantfuckyourself", "&cYou cant invite yourself"));
            return;
        }
        if(Bukkit.getPlayer(sender.getName()) == null) {
            Thread.ofVirtual().start(() -> sender.sendMessage("chatp.invite.cantfind", "Cant find this player"));
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

        MDatabase.getPlayerTag(senderUUID).thenCompose(tag -> {
            if(!tag.equalsIgnoreCase("admin") && !tag.equalsIgnoreCase("staff")) {
                Thread.ofVirtual().start(() -> sender.sendMessage("chatp.invite.cant","&cYou need to be admin or staff to invite"));
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.getPlayerGroup(senderUUID);
        }).thenAccept(group -> {
            MDatabase.setPlayerInvited(argUUID, true);
            TextComponent msg = new TextComponent(ColorUtils.C(sender.getName() + " Invited you to " + group + " \n&eClick this messaage to join"));
            msg.setColor(ChatColor.RED);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.CUSTOM, "/chatp invite " + args[0] + " " + group)); // chatp invite playerName
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click this to join to the group").create()));
            Thread.ofVirtual().start(() -> sender.spigot().sendMessage(msg));
        }).exceptionallyAsync(exp -> {
            sender.sendMessage(ColorUtils.C((String) ChatPlugin.configuration.get("chatp.invite.error", "&cAn error occurred, check if you are in a group atleast")));
            throw new RuntimeException(exp);
        });
    }
}
