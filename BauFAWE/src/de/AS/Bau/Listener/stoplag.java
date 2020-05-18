package de.AS.Bau.Listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;

public class stoplag implements Listener {
	public static List<String> pasteSL = new ArrayList<>(); //uuid.PLot

	public stoplag(Main plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void pistonextend(BlockPistonExtendEvent e) {
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
			}else if(pasteSL.contains(e.getBlock().getWorld().getName() + "." + rgID)){
				e.setCancelled(true);
			}
		}

	}
 
	@EventHandler(priority = EventPriority.LOWEST)
	public static void pistonRetract(BlockPistonRetractEvent e) {
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
			}else if(pasteSL.contains(e.getBlock().getWorld().getName() + "." + rgID)){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void redstone(BlockRedstoneEvent e) {
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
					if (e.getNewCurrent() >= 1 && e.getOldCurrent() < 1) {
						e.setNewCurrent(0);
					}
				}else if(pasteSL.contains(e.getBlock().getWorld().getName() + "." + rgID)){
					if (e.getNewCurrent() >= 1 && e.getOldCurrent() < 1) {
						e.setNewCurrent(0);
					}
				}
			}
		}

	}

	public static void setan(boolean option, String worldName, String plot) {
		Main main = Main.getPlugin();
		main.getCustomConfig().set("stoplag." + worldName + "." + plot, "an");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public static void onBlockFade(BlockFadeEvent e) {
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
	public static void onBlockForm(BlockFormEvent e) {
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

	/* permanent canceled Events */
	
	@EventHandler
	public void BlockSpread(BlockSpreadEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void LeavedDecay(LeavesDecayEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void SpongeAbsorb(SpongeAbsorbEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void BlockGrow(BlockGrowEvent e) {
		e.setCancelled(true);
	}
}
