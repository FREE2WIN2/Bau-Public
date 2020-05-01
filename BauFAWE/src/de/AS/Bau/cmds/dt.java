package de.AS.Bau.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.HashMap;
import org.bukkit.entity.Player;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Scoreboard.ScoreBoardBau;


public class dt implements CommandExecutor {
public static HashMap<Player, Boolean>playerHasDtOn = new HashMap<>(); 

@Override
public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
				Player p = (Player) sender;
				if(playerHasDtOn.get(p)) {
					playerHasDtOn.put(p,false);
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "dtOff"));
					ScoreBoardBau.cmdUpdate(p);
					return true;
				}else {
					playerHasDtOn.put(p,true);
					p.sendMessage(Main.prefix + StringGetterBau.getString(p,"dtOn"));
					ScoreBoardBau.cmdUpdate(p);
					return true;
				}
	}

}
