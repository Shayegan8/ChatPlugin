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
        if (sender instanceof Player && sender.hasPermission(getPermission())) {
            if (args.length == 2) {
                CompletableFuture<String> senderInfo = MDatabase.getPlayerGroup(sender.getName());
                senderInfo.thenAcceptAsync((obj) -> {
                    TextComponent msg = new TextComponent(ColorUtils.C(sender.getName() + " Invited you to " + obj + " \n&eClick this messaage to join"));
                    msg.setColor(ChatColor.RED);
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.CUSTOM, "/chatp add " + args[1] + " " + obj)); // chatp add playerName group
                    msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click this to join to the group").create()));
                    sender.spigot().sendMessage(msg);
                });
            } else {
                sender.sendMessage(getUsage());
            }
        } else {
            sender.sendMessage(getPermissionMSG());
        }
    }
}
