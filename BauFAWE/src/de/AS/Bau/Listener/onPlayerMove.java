package de.AS.Bau.Listener;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import de.AS.Bau.Scoreboard.ScoreBoardBau;

public class onPlayerMove implements Listener {
	public static HashMap<Player, String> playersLastPlot = new HashMap<>();

	public onPlayerMove(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e) {

		Player p = e.getPlayer();
		if (!p.getWorld().getName().equals("world")) {
			Location loc = p.getLocation();
			RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
					.get(BukkitAdapter.adapt(p.getWorld()));
			List<String> regionsIDs = rm.getApplicableRegionsIDs(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
			if (!regionsIDs.isEmpty()) {
				String rgID = regionsIDs.get(0);
				if (!rgID.equals(playersLastPlot.get(p)) && !rgID.equals("allplots")) {
					playersLastPlot.put(p, rgID);
					//System.out.println(rgID);
					ScoreBoardBau.cmdUpdate(p);
				}
			}
			loc = e.getTo();
		  if(loc.getBlockX()>-34||loc.getBlockX()<-381||loc.getBlockY()<5||loc.
		  getBlockY()>81||loc.getBlockZ()<-78||loc.getBlockZ()>111) {
				p.teleport(e.getFrom());
			}

		}

	}
}
