package de.AS.Bau.cmds;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Scoreboard.ScoreBoardBau;
import de.AS.Bau.Tools.Stoplag;

public class sl implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		Player p = (Player) sender;

		if (args.length == 0) {
			boolean on = Stoplag.getStatus(p.getLocation());
			Stoplag.setStatus(p.getLocation(), !on);

			if (on) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOff"));
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));
			}
			sendToAll(p);
			return true;			
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				Stoplag.setStatus(p.getLocation(), true);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));
			} else if (args[0].equalsIgnoreCase("aus") || args[0].equalsIgnoreCase("off")) {
				Stoplag.setStatus(p.getLocation(), false);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOff"));
			}
			sendToAll(p);
			return true;
		} else {
			return false;
		}

	}

	private void sendToAll(Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				for (Player b : p.getWorld().getPlayers()) {
					ScoreBoardBau.cmdUpdate(b);
				}
			}
		}, 5);

	}

}
