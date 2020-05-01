package de.AS.Bau.TabCompleter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.AS.Bau.DBConnection;

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
				out.add("time");
				return out;
			} else if (args.length == 2) {
				DBConnection conn = new DBConnection();
				ResultSet rs;
				switch (args[0]) {
				case "tp":
					rs = conn.getMemberedPlots(p);
					try {
						while (rs.next()) {
							out.add(conn.getName(rs.getString(1)));
						}
						conn.closeConn();
						return out;
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
					break;
				case "add":
				//case "addtemp":
					for (Player a : Bukkit.getServer().getOnlinePlayers()) {
						out.add(a.getName());
						
					}
					out.remove(p.getName());
					conn.closeConn();
					return out;
				case "remove":
					rs = conn.getMember(p.getUniqueId().toString());
					try {
						while (rs.next()) {
							out.add(conn.getName(rs.getString(1)));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					conn.closeConn();
					return out;
				}
				conn.closeConn();
			}

		}
		return out;
	}


}
