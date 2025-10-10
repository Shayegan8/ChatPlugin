package shayegan8.github.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shayegan8.github.ChatPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseCommand implements CommandExecutor, TabExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
			String[] args) {
		if (args.length == 0) {
			ChatPlugin.sendABMSG(sender, "chatp.usage", "&c/chatp help to get instruction for all commands");
			return true;
		}

		CommandManager cmd_ = ChatPlugin.commands.get(args[0].toLowerCase());
		if (cmd_ == null)
			return true;

		if (!sender.hasPermission(cmd_.getPermission())) {
			ChatPlugin.sendABMSG(sender, "chatp.permissionMSG", "&cYou dont have a permission!");
			return true;
		}

		cmd_.execute(sender, Arrays.copyOfRange(args, 1, args.length));

		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
			String[] args) {
		String firstArgument = args[0];
		return switch (firstArgument) {
		case "group" -> switch (args.length) { // chatp group create name
		case 2 ->
			Stream.of("create", "delete", "help").filter((x) -> x.startsWith(args[1])).collect(Collectors.toList());
		default -> List.of();
		};
		case "invite", "request", "accept" -> switch (args.length) {
		case 2 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).filter((each) -> each.startsWith(args[1]))
				.collect(Collectors.toList());
		default -> List.of();
		};
		default -> Stream.of("remove", "reload", "quit", "mute", "join", "invite", "help", "group", "friends", "menu",
				"request", "accept").filter((x) -> x.startsWith(firstArgument)).collect(Collectors.toList());
		};
	}
}
