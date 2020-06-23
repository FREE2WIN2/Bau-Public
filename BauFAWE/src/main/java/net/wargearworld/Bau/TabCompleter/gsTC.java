package net.wargearworld.Bau.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.utils.HelperMethods;

public class gsTC implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String string, String[] args) {
		ArrayList<String> out = new ArrayList<>();
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 1) {
				out.add("info");
				out.add("list");
				out.add("tp");
				out.add("add");
				out.add("addtemp");
				out.add("time");
				out.add("remove");
				if(p.hasPermission("admin")) {
					out.add("delete");
				}
				return HelperMethods.checkFortiped(args[0], out);
			} else if (args.length == 2) {
				switch (args[0]) {
				case "delete":
					if(!p.hasPermission("admin")) {
						return out;
					}
					out.addAll(DBConnection.getAllWorlds());
					return HelperMethods.checkFortiped(args[1], out);
				case "tp":
					out.addAll(DBConnection.getMemberedPlots(p.getUniqueId()));
					break;
				case "add":
				//case "addtemp":
					for (Player a : Bukkit.getServer().getOnlinePlayers()) {
						out.add(a.getName());
						
					}
					out.remove(p.getName());
					return HelperMethods.checkFortiped(args[1], out);
				case "remove":
					
					for(String uuidMember:DBConnection.getMember(p.getUniqueId().toString())) {
						out.add(DBConnection.getName(uuidMember));
					}
					return HelperMethods.checkFortiped(args[1], out);
				}
			}

		}
		return out;
	}

}
