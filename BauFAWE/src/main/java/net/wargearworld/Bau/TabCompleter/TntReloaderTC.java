package net.wargearworld.Bau.TabCompleter;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.wargearworld.Bau.utils.HelperMethods;

public class TntReloaderTC implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String string, String[] args) {
		List<String> out = new LinkedList<>();
		if(args.length == 1) {
			out.add("help");
			out.add("start");
			out.add("stop");
			out.add("paste");
			out.add("reset");
			return HelperMethods.checkFortiped(args[0], out);
		}
		return null;
	}



}
