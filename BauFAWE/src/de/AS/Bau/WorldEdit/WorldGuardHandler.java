package de.AS.Bau.WorldEdit;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class WorldGuardHandler {
	static RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

	public static String getPlotId(BlockVector3 location, World world) {
		RegionManager regions = container.get(world);
		return regions.getApplicableRegionsIDs(location).get(0);
	}

	public static String getPlotId(Location loc) {
		return getPlotId(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),BukkitAdapter.adapt(loc.getWorld()));
	}
}