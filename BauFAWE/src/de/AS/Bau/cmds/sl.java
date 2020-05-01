package de.AS.Bau.cmds;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Scoreboard.ScoreBoardBau;

public class sl implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		Player p = (Player) sender;
		Main main = Main.getPlugin();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		String rgID = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())).get(0);
		if (args.length == 0) {
			if (main.getCustomConfig().getString("stoplag." + p.getWorld().getName() + "." + rgID)
					.equalsIgnoreCase("an")) {
				main.getCustomConfig().set("stoplag." + p.getWorld().getName() + "." + rgID, "aus");
				try {
					main.getCustomConfig().save(main.getCustomConfigFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOff"));

			} else {
				main.getCustomConfig().set("stoplag." + p.getWorld().getName() + "." + rgID, "an");
				try {
					main.getCustomConfig().save(main.getCustomConfigFile());
				} catch (IOException e) {
					e.printStackTrace();
				}

				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));

			}
			sendToAll(p);
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				main.getCustomConfig().set("stoplag." + p.getWorld().getName() + "." + rgID, "an");
				try {
					main.getCustomConfig().save(main.getCustomConfigFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));
			} else if (args[0].equalsIgnoreCase("aus") || args[0].equalsIgnoreCase("off")) {
				main.getCustomConfig().set("stoplag." + p.getWorld().getName() + "." + rgID, "aus");
				try {
					main.getCustomConfig().save(main.getCustomConfigFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
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
				for(Player b:p.getWorld().getPlayers()) {
					ScoreBoardBau.cmdUpdate(b);
				}
			}
		}, 5);
		
		
	}

}
