package de.AS.Bau.Listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.Scoreboard.ScoreBoardBau;
import de.AS.Bau.WorldEdit.WorldGuardHandler;

public class onPlayerMove implements Listener {
	public static HashMap<Player, String> playersLastPlot = new HashMap<>();

	public onPlayerMove(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e) {

		Player p = e.getPlayer();
		if (!p.getWorld().getName().equals("world")) {

			String rgID = WorldGuardHandler.getPlotId(p.getLocation());
			/*You have to be in a Region to move(make the Region out of the regions so big.)*/
			
			if (rgID == null&&!p.hasPermission("bau.move.bypass")) {
				p.teleport(e.getFrom());
				e.setCancelled(true);
				return;
			}
			if(rgID == null) {
				return;
			}
			if (!rgID.equals(playersLastPlot.get(p)) && !rgID.equals("allplots")) {
				playersLastPlot.put(p, rgID);
				ScoreBoardBau.cmdUpdate(p);
			}

		}

	}
}
