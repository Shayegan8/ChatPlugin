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

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Invite extends CommandManager {

    @Override
    public String getUsage() {
        return "&e/chatp invite playername";
    }

    @Override
    public String getPermission() {
        return "chatp.base.invite";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
       if (!(sender instanceof Player player)) {
           Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.B(ChatPlugin.configuration.getString("chatp.invite.notPlayer", "&cYou should be a player"))));
           return;
       }
       if (args.length != 1) {
           Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.invite.usage", getUsage()))));
           return;
       }
       if(sender.getName().equalsIgnoreCase(args[0])) {
           Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.invite.cantFuckYourself", "&cYou cant invite yourself"))));
           return;
       }
       if(Bukkit.getPlayer(args[0]) == null) {
           Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.invite.cantFind", "&cCant find this player"))));
           return;
       }
       UUID senderUUID = player.getUniqueId();
       UUID argUUID = Bukkit.getPlayer(args[0]).getUniqueId();
       MDatabase.getPlayerTag(senderUUID).thenCompose(tag -> {
           if(!tag.equalsIgnoreCase("admin") && !tag.equalsIgnoreCase("staff")) {
               Thread.ofVirtual().start(() -> sender.sendMessage("chatp.invite.cant", "&cYou need to be admin or staff to invite"));
               return CompletableFuture.completedFuture(null);
           }
           return MDatabase.getPlayerGroup(senderUUID);
       }).thenAccept(group -> {
           MDatabase.setPlayerInvited(argUUID, true);
           Thread.ofVirtual().start(() -> {
               TextComponent msg = new TextComponent(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.invite.joinMSG", "%player_name% Invited you to %group_name%\n&eClick this messaage to join")));
               msg.setColor(ChatColor.RED);
               msg.setClickEvent(new ClickEvent(ClickEvent.Action.CUSTOM, "/chatp join " + group));
               msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.invite.bar", "&eClick this to join to the group"))).create()));
               sender.spigot().sendMessage(msg);
           });
       }).exceptionallyAsync(exp -> {
           sender.sendMessage(ColorUtils.C(player, ChatPlugin.configuration.getString("chatp.invite.error", "&cAn error occurred, check if you are in a group")));
           throw new IllegalStateException(Arrays.toString(exp.getStackTrace()));
       });
    }
}
