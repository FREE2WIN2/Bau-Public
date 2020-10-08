package net.wargearworld.bau.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.bau.world.WorldManager;


public class onPlayerRespawn implements Listener {
	public onPlayerRespawn(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawnevent(PlayerRespawnEvent e) {
		WorldManager.get(e.getPlayer().getWorld()).spawn(e.getPlayer());
	}
}
