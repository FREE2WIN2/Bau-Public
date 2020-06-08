package net.wargearworld.Bau.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.wargearworld.Bau.Main;

public class Bau implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(sender.hasPermission("moderator")) {
			if(args.length == 0) {
				sender.sendMessage(Main.prefix + "BauPlugin reload");
			}
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("reload")) {
					Main.getPlugin().createConfigs();
					sender.sendMessage(Main.prefix + "Configs successfully reloaded!");
				}
			}
			}
			return true;
	}

}
