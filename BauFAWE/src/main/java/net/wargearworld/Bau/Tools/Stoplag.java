package net.wargearworld.Bau.Tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
import net.wargearworld.Bau.utils.HelperMethods;

public class Stoplag implements Listener, TabExecutor {
	public static File stoplagConfigFile;
	public static YamlConfiguration stoplagConfig;
	private static HashMap<String, Boolean> stoplagBefore = new HashMap<>();

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
			// sl paste -> toggle

			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				setStatus(p.getLocation(), true);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOn"));
			} else if (args[0].equalsIgnoreCase("aus") || args[0].equalsIgnoreCase("off")) {
				setStatus(p.getLocation(), false);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "slOff"));
			} else if (args[0].equalsIgnoreCase("paste")) {
				setPasteState(p.getLocation(), !getPasteState(p.getLocation()));
			}
			return true;
		} else if (args.length == 2) {
			// sl paste on|off
			// sl paste <time>
			if (!args[0].equalsIgnoreCase("paste")) {
				return true;
			}
			if (args[1].equals("on") || args[1].equals("an")) {
				setPasteState(p.getLocation(), true);
				Main.send(p, "stoplag_pasteOn");
			} else if (args[1].equals("off") || args[1].equals("aus")) {
				setPasteState(p.getLocation(), false);
				Main.send(p, "stoplag_pasteOff");
			} else if (HelperMethods.isInt(args[1])) {
				setPasteTime(p.getLocation(), Integer.parseInt(args[1]));
				Main.send(p, "stoplag_pasteTime", args[1]);
			}
			return true;
		} else {
			return false;
		}

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> out = new LinkedList<>();
		if (!(sender instanceof Player)) {
			return out;
		}
		if (args.length == 1) {
			out.add("on");
			out.add("off");
			out.add("an");
			out.add("aus");
			out.add("paste");
			return HelperMethods.checkFortiped(args[0], out);
		} else if (args.length == 2) {
			switch (args[0]) {
			case "paste":
				out.add("on");
				out.add("off");
				out.add("an");
				out.add("aus");
				return HelperMethods.checkFortiped(args[1], out);
			}
		}

		return out;
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
		World w = Bukkit.getWorld(worldName);
		if (w != null) {
			for (Player player : w.getPlayers()) {
				ScoreBoardBau.cmdUpdate(player);
			}
		}
		return true;
	}

	/* easier acess */

	public static boolean setStatus(Location loc, boolean on) {
		String rgID = WorldGuardHandler.getPlotId(loc);
		return setStatus(loc.getWorld().getName(), rgID, on);

	}

	public static boolean setStatusTemp(String worldName, String regionID, boolean on, int time) {
		if (!stoplagBefore.containsKey(worldName + "_" + regionID)) {
			stoplagBefore.put(worldName + "_" + regionID, getStatus(worldName, regionID));
		}
		boolean stateBefore = stoplagBefore.get(worldName + "_" + regionID);
		/* remove after time secs */
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				stoplagBefore.remove(worldName + "_" + regionID);
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

	/* Paste */

	public static void setPasteState(Location loc, boolean on) {
		String worldName = loc.getWorld().getName();
		stoplagConfig.set(worldName + ".paste", on);
		try {
			stoplagConfig.save(stoplagConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean getPasteState(Location loc) {
		String worldName = loc.getWorld().getName();
		if (!stoplagConfig.contains(worldName + ".paste")) {
			setPasteState(loc, true);
		}

		return stoplagConfig.getBoolean(worldName + ".paste");
	}

	public static void setPasteTime(Location loc, int time) {
		String worldName = loc.getWorld().getName();
		stoplagConfig.set(worldName + ".pasteTime", time);
		try {
			stoplagConfig.save(stoplagConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getPasteTime(Location loc) {
		String worldName = loc.getWorld().getName();
		if (!stoplagConfig.contains(worldName + ".pasteTime")) {
			setPasteTime(loc, 5);
		}

		return stoplagConfig.getInt(worldName + ".pasteTime");
	}
	/* Events for Stoplag */

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void pistonextend(BlockPistonExtendEvent e) {
		if(getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
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
