package de.AS.Bau.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.AS.Bau.Listener.OnInvClick;

public class delcon implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		if(sender instanceof Player && args.length == 2) {
			Player p = (Player) sender;
			if(args[1].equals(p.getUniqueId().toString())) {
				OnInvClick.resetRegion(args[0], p, true);
				return true;
			}
		}
		return false;
	}

}
