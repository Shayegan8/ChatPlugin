package shayegan8.github.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shayegan8.github.ColorUtils;
import shayegan8.github.database.MDatabase;

import java.util.UUID;
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
        if (args.length != 1) {
            sender.sendMessage(getUsage());
            return;
        }
        if(sender.getName().equalsIgnoreCase(args[0])) {
            sender.sendMessage("You cant invite yourself");
            return;
        }
        if(Bukkit.getPlayer(sender.getName()) == null) {
            sender.sendMessage("Cant find this player");
            return;
        }
        UUID senderUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
        UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();

        MDatabase.getPlayerTag(senderUUID).thenCompose((tag) -> {
            if(!tag.equalsIgnoreCase("admin") && !tag.equalsIgnoreCase("staff")) {
                sender.sendMessage("You need to be admin or staff to invite");
                return CompletableFuture.completedFuture(null);
            }
            return MDatabase.getPlayerGroup(senderUUID);
        }).thenAccept((group) -> {
            MDatabase.setPlayerInvited(argUUID, true);
            TextComponent msg = new TextComponent(ColorUtils.C(sender.getName() + " Invited you to " + group + " \n&eClick this messaage to join"));
            msg.setColor(ChatColor.RED);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.CUSTOM, "/chatp invite " + args[0] + " " + group)); // chatp invite playerName
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click this to join to the group").create()));
            sender.spigot().sendMessage(msg);
        }).exceptionally((exp) -> {
            sender.sendMessage("An error occurred, check if you are in a group atleast");
            throw new IllegalStateException(exp);
        });
    }
}
