package de.AS.Bau.Tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;
import de.AS.Bau.WorldEdit.WorldGuardHandler;

public class Stoplag implements Listener {
	public static File stoplagConfigFile;
	public static YamlConfiguration stoplagConfig;

	public static boolean setStatus(String worldName, String regionID, boolean on) {
		if (worldName == null || regionID == null) {
			return false;
		}
		stoplagConfig.set(worldName + "." + regionID, on);
		try {
			stoplagConfig.save(stoplagConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/* easier acess */

	public static boolean setStatus(Location loc, boolean on) {
		String rgID = WorldGuardHandler.getPlotId(loc);
		return setStatus(loc.getWorld().getName(), rgID, on);

	}

	public static boolean setStatusTemp(String worldName, String regionID, boolean on, int time) {
		boolean stateBefore = getStatus(worldName, regionID);
		/* remove after time secs */
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				setStatus(worldName, regionID, stateBefore);
			}
		}, 20 * time);

		/* set status */

		return setStatus(worldName, regionID, on);
	}

	public static boolean setStatusTemp(Location loc, boolean on, int time) {
		return setStatusTemp(loc.getWorld().getName(), WorldGuardHandler.getPlotId(loc), on, time);
	}

	/* get status */

	public static boolean getStatus(String worldName, String regionID) {
		if (regionID == null || worldName == null) {
			return false;
		}

		if (!stoplagConfig.contains(worldName + "." + regionID)) {
			return false;
		}

		return stoplagConfig.getBoolean(worldName + "." + regionID);
	}

	/* easier acess */

	public static boolean getStatus(Location loc) {
		return getStatus(loc.getWorld().getName(), WorldGuardHandler.getPlotId(loc));
	}

	/* Events for Stoplag */

	@EventHandler(priority = EventPriority.LOWEST)
	public static void pistonextend(BlockPistonExtendEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void pistonRetract(BlockPistonRetractEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void redstone(BlockRedstoneEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			if (e.getNewCurrent() >= 1 && e.getOldCurrent() < 1) {
				e.setNewCurrent(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockFade(BlockFadeEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockForm(BlockFormEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockPhysics(BlockPhysicsEvent e) {
		// wenn zb unter redstone block abgebrut wird

		Main main = Main.getPlugin();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(e.getBlock().getWorld()));
		List<String> regionsIDs = regions.getApplicableRegionsIDs(
				BlockVector3.at(e.getBlock().getLocation().getX(), 10, e.getBlock().getLocation().getZ()));
		if (!regionsIDs.isEmpty()) {
			String rgID = regionsIDs.get(0);
			if (rgID.contains("plot") && !rgID.equals("allplots")) {
				if (main.getCustomConfig().getString("stoplag." + e.getBlock().getWorld().getName() + "." + rgID)
						.equals("an")) {
					e.setCancelled(true);
				}
			}

		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockFromTo(BlockFromToEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}
}
