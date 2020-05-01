package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class onPlayerRespawn implements Listener {
	public onPlayerRespawn(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeaveevent(PlayerRespawnEvent e) {
		World w = onPlayerJoin.loadWorld(e.getPlayer().getUniqueId().toString());
		e.setRespawnLocation(new Location(w, -208.5, 8, 17));
	}
}
