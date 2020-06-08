package net.wargearworld.Bau.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.wargearworld.Bau.utils.HelperMethods;

public class tbsTC implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String string, String[] args) {
		List<String> out = new ArrayList<>();
		if(args.length ==1) {
			out.add("last");
			out.add("undo");
			out.add("1");
			out.add("2");
			out.add("3");
		}else if(args.length == 2) {
			out.add("N");
			out.add("S");
		}else if(args.length == 3) {
			out.add("S");
			out.add("N");
			out.add("R");
			out.add("F");
		}
		return HelperMethods.checkFortiped(args[args.length-1], out);
	}

}
