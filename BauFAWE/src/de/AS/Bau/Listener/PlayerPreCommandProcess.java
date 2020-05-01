package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;

public class PlayerPreCommandProcess implements Listener {
	public PlayerPreCommandProcess() {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());
	}

	@EventHandler
	public void nopaste(PlayerCommandPreprocessEvent e) {
		String cmd = e.getMessage().toLowerCase();

		if (cmd.startsWith("//paste")) {
			Player p = e.getPlayer();
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
			String rgID = regions
					.getApplicableRegionsIDs(
							BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
					.get(0);
			stoplag.pasteSL.add(p.getWorld().getName() + "."+rgID);
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						stoplag.pasteSL.remove(p.getWorld().getName() + "."+rgID);
					}
				}, 40);

		}

	}
}
