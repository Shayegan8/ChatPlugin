package shayegan8.github.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Invite extends CommandManager {

    @Override
    public String getUsage() {
        return "/chatp invite `playerName`";
    }

    @Override
    public String getPermissionMSG() {
        return "You do not have a permission!";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getPermission() {
        return "chatp.base.invite";
    }

    @Override
    public String getDescription() {
        return "chatp invite command";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) && !(sender.hasPermission(getPermission()))) {
            sender.sendMessage(getPermissionMSG());
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(getUsage());
            return;
        }
        MDatabase.getPlayerTag(sender.getName()).thenCompose((tag) -> {
            if(!tag.equalsIgnoreCase("admin") && !tag.equalsIgnoreCase("staff")) {
                sender.sendMessage("You need to be admin or staff to invite");
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.getPlayerGroup(sender.getName());
        }).thenAccept((group) -> {
            TextComponent msg = new TextComponent(ColorUtils.C(sender.getName() + " Invited you to " + group + " \n&eClick this messaage to join"));
            msg.setColor(ChatColor.RED);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.CUSTOM, "/chatp join " + args[1] + " " + group)); // chatp add playerName group
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click this to join to the group").create()));
            sender.spigot().sendMessage(msg);
        }).exceptionally((exp) -> {
            sender.sendMessage("An error occurred");
            throw new IllegalStateException(exp.getMessage());
        });
    }
}
