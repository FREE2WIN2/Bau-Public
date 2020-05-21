package de.AS.Bau.WorldEdit;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class WorldGuardHandler {
	static RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

	public static String getPlotId(BlockVector3 location, World world) {
		RegionManager regions = container.get(world);
		if(regions.getApplicableRegionsIDs(location).size() == 0) {
			return null;
		}
		return regions.getApplicableRegionsIDs(location).get(0);
	}

	public static String getPlotId(Location loc) {
		return getPlotId(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),BukkitAdapter.adapt(loc.getWorld()));
	}
	
	public static ProtectedRegion getRegion(String regionID, World world) {
		RegionManager regions = container.get(world);
		return regions.getRegion(regionID);
	}
	
	public static ProtectedRegion getRegion(Location loc) {
		return getRegion(getPlotId(loc), BukkitAdapter.adapt(loc.getWorld()));
	}
}