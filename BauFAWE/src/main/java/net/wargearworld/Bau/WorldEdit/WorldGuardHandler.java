package net.wargearworld.Bau.WorldEdit;

import java.util.UUID;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.utils.WorldHandler;

public class WorldGuardHandler {
	static RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

	public static String getPlotId(BlockVector3 location, World world) {
		RegionManager regions = container.get(world);
		if (regions.getApplicableRegionsIDs(location).size() == 0) {
			return null;
		}
		return regions.getApplicableRegionsIDs(location).get(0);
	}

	public static String getPlotId(Location loc) {
		return getPlotId(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
				BukkitAdapter.adapt(loc.getWorld()));
	}

	public static ProtectedRegion getRegion(String regionID, World world) {
		RegionManager regions = container.get(world);
		return regions.getRegion(regionID);
	}

	public static ProtectedRegion getRegion(Location loc) {
		return getRegion(getPlotId(loc), BukkitAdapter.adapt(loc.getWorld()));
	}

	public static boolean isInBuildRegion(Location loc) {
		return isInBuildRegion(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()),
				BukkitAdapter.adapt(loc.getWorld()));
	}

	public static boolean isInBuildRegion(BlockVector3 location, World world) {
		RegionManager regions = container.get(world);
		if (regions.getApplicableRegionsIDs(location).size() <= 1) {
			return false;
		}
		return regions.getApplicableRegionsIDs(location).size() > 1;
	}

	public static boolean addPlayerToAllRegions(String worldName, String playerUUID) {
		RegionManager regions = container.get(BukkitAdapter.adapt(WorldHandler.loadWorld(worldName)));
		for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
			if (rg.getValue().getPriority() > 10) {
				DefaultDomain member = rg.getValue().getMembers();
				member.addPlayer(UUID.fromString(playerUUID));
				rg.getValue().setMembers(member);
			}
		}
		return true;
	}

	public static boolean removeMemberFromAllRegions(String worldName, UUID playerUUID) {
		RegionManager regions = container.get(BukkitAdapter.adapt(WorldHandler.loadWorld(worldName)));
		for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
			if (rg.getValue().getPriority() > 10) {
				DefaultDomain member = rg.getValue().getMembers();

				member.removePlayer(DBConnection.getName(playerUUID.toString()));

				member.removePlayer(playerUUID);
				rg.getValue().setMembers(member);
			}
		}
		return true;
	}

	public static ProtectedRegion getBuildRegion(Location loc) {
		RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));
		List<String> ids = regions.getApplicableRegionsIDs(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
		if (ids.size() <= 2) {
			return regions.getRegion(ids.get(0));
		}
		return regions.getRegion(ids.get(1));

	}

	
	public static void addOwnerToAllRegions(UUID uniqueId) {
		RegionManager regions = container.get(BukkitAdapter.adapt(WorldHandler.loadWorld(uniqueId.toString())));
		for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
			if (rg.getValue().getPriority() > 10) {
				DefaultDomain owner = rg.getValue().getOwners();
				owner.addPlayer(uniqueId);
				rg.getValue().setOwners(owner);
			}
		}

	}
}