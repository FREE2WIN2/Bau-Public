package net.wargearworld.Bau.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;

import com.sk89q.worldedit.util.Location;

import com.sk89q.worldguard.WorldGuard;

import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import net.wargearworld.Bau.Main;

public class ExplosioneventListener implements Listener {
	public ExplosioneventListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTntExplosion(EntityExplodeEvent event) {

		Entity e = event.getEntity();
		Location loc = BukkitAdapter.adapt(e.getLocation());
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		if (!query.testState(loc, null, Main.TntExplosion)) {
			event.blockList().clear();
		}

	}
}
