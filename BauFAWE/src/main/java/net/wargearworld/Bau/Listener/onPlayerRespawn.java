package net.wargearworld.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Plots.Plots;
import net.wargearworld.Bau.utils.CoordGetter;

public class onPlayerRespawn implements Listener {
	public onPlayerRespawn(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeaveevent(PlayerRespawnEvent e) {
		e.setRespawnLocation(CoordGetter.getTeleportLocation(e.getPlayer().getWorld(), Plots.getJoinPlot(e.getPlayer().getUniqueId())));
	}
}
