package shayegan8.github.commands;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import shayegan8.github.ChatPlugin;
import shayegan8.github.ColorUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class BaseCommand implements CommandExecutor, TabExecutor {

    private CooldownManager cooldownManager;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C((Player)sender, (String) ChatPlugin.configuration
                    .get("chatp.usage", "&c/chatp help to get instruction for all commands"))));
            return true;
        }

        CommandManager cmd_ = ChatPlugin.commands.get(args[0].toLowerCase());
        if(cmd_ == null)
            return true;

        CompletableFuture.supplyAsync(() -> ChatPlugin.configuration.getBoolean("cooldown", false)).thenAccept((bool) -> {
            Thread.ofVirtual().start(() -> {
                if(!bool)
                    return;
                if(cooldownManager.hasCooldown(sender.getName())) {
                    sender.sendMessage(String.valueOf(cooldownManager.getRemained(sender.getName())));
                }
                cooldownManager.setCooldown(sender.getName(), Duration.ofSeconds(60));
            });
        });

        if(!sender.hasPermission(cmd_.getPermission())) {
            Thread.ofVirtual().start(() -> sender.sendMessage(ColorUtils.C((Player)sender, ChatPlugin.configuration.getString("chatp.permissionMSG", "&cYou dont have a permission!"))));
            return true;
        }

        cmd_.execute(sender, Arrays.copyOfRange(args, 1, args.length));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        String firstArgument = args[0];
        return switch (firstArgument) {
            case "group" -> switch (args.length) { //chatp group create name
                case 1 ->
                        Stream.of("create", "delete", "help").filter((x) -> x.startsWith(args[0])).collect(Collectors.toList());
                case 2 ->
                        Bukkit.getOnlinePlayers().stream().map(Player::getName).filter((each) -> each.startsWith(args[1])).collect(Collectors.toList());
                default -> Stream.of("no player found").collect(Collectors.toList());
            };
            case "join", "invite" -> switch (args.length) {
                case 1 ->
                        Bukkit.getOnlinePlayers().stream().map(Player::getName).filter((each) -> each.startsWith(args[0])).collect(Collectors.toList());
                default -> Stream.of("no player found").collect(Collectors.toList());
            };
            default ->
                    Stream.of("remove", "reload", "quit", "mute", "join", "invite", "help", "group", "friends").filter((x) -> x.startsWith(firstArgument)).collect(Collectors.toList());
        };
    }
}
