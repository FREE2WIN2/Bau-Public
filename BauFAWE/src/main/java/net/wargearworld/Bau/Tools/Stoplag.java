package net.wargearworld.Bau.Tools;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import org.bukkit.event.entity.ExplosionPrimeEvent;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.StringGetterBau;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;

public class Stoplag implements Listener,CommandExecutor {
	public static File stoplagConfigFile;
	public static YamlConfiguration stoplagConfig;


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		Player p = (Player) sender;

		if (args.length == 0) {
			boolean on = Stoplag.getStatus(p.getLocation());
			setStatus(p.getLocation(), !on);

			if (on) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOff"));
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));
			}
			sendToAll(p);
			return true;			
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				setStatus(p.getLocation(), true);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));
			} else if (args[0].equalsIgnoreCase("aus") || args[0].equalsIgnoreCase("off")) {
				setStatus(p.getLocation(), false);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOff"));
			}
			sendToAll(p);
			return true;
		} else {
			return false;
		}

	}

	private void sendToAll(Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				for (Player b : p.getWorld().getPlayers()) {
					ScoreBoardBau.cmdUpdate(b);
				}
			}
		}, 5);

	}

	
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void pistonextend(BlockPistonExtendEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void pistonRetract(BlockPistonRetractEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void redstone(BlockRedstoneEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			if (e.getNewCurrent() >= 1 && e.getOldCurrent() < 1) {
				e.setNewCurrent(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onBlockFade(BlockFadeEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onBlockForm(BlockFormEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onBlockPhysics(BlockPhysicsEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onBlockFromTo(BlockFromToEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	public void entityPrime(ExplosionPrimeEvent e) {
		e.setCancelled(getStatus(e.getEntity().getLocation()));
	}
}
