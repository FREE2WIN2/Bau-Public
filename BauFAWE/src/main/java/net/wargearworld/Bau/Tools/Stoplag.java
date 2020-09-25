package net.wargearworld.Bau.Tools;

import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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

import com.sk89q.worldedit.math.BlockVector3;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.World.BauWorld;
import net.wargearworld.Bau.World.Plot;
import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;
import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.Arguments.IntegerArgument;

public class Stoplag implements Listener, TabExecutor {
	private static Stoplag instance;

	public static Stoplag getInstance() {
		return instance;
	}

	private CommandHandel commandHandle;

	public Stoplag() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
		Main.getPlugin().getCommand("sl").setExecutor(this);
		commandHandle = new CommandHandel("sl", Main.prefix);

		commandHandle.setCallback(s -> {
			toggleSL(s);
		});

		commandHandle.addSubNode(literal("an").setCallback(s -> {
			setSL(true, s.getPlayer());
		}));
		commandHandle.addSubNode(literal("aus").setCallback(s -> {
			setSL(false, s.getPlayer());
		}));

		commandHandle.addSubNode(literal("paste").addSubNode(literal("an")).setCallback(s -> {
			setSLPaste(true, s.getPlayer());
		}).addSubNode(literal("aus")).setCallback(s -> {
			setSLPaste(false, s.getPlayer());
		}).addSubNode(argument("time", new IntegerArgument())).setCallback(s -> {
			setSLPasteTime(s);
		}));
	}

	public void setSLPasteTime(ArgumentList s) {
		Player p = s.getPlayer();
		BauPlayer player = BauPlayer.getBauPlayer(p);
		player.setSLPasteTime(s.getInt("time"));
		Main.send(p, "stoplag_pasteTime", s.getInt("time") + "");
	}

	public void setSLPaste(boolean b, Player p) {
		BauPlayer player = BauPlayer.getBauPlayer(p);
		player.setSLPaste(b);
		if (b) {
			Main.send(p, "stoplag_pasteOn");
		} else {
			Main.send(p, "stoplag_pasteOff");
		}
	}

	public void setStatusTemp(Location loc, boolean on, int time) {
		Plot plot = WorldManager.get(loc.getWorld()).getPlot(loc);
		boolean stateBefore = plot.getSL();
		plot.setSL(true);
		/* remove after time secs */
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				plot.setSL(stateBefore);
			}
		}, 20 * time);
	}

	public void setSL(boolean on, Player p) {
		BauWorld world = WorldManager.get(p.getWorld());
		Plot plot = world.getPlot(p.getLocation());

		plot.setSL(on);
		if (on) {// is now active
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "slOn"));
		} else {
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "slOff"));
		}
		sendToAll(p);
	}

	private void toggleSL(ArgumentList s) {
		Player p = s.getPlayer();
		BauWorld world = WorldManager.get(p.getWorld());
		BlockVector3 pos = BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
		Plot plot = world.getPlot(world.getRegionManager().getApplicableRegionsIDs(pos).get(0));
		plot.setSL(!plot.getSL());
		if (plot.getSL()) {// is now active
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "slOn"));
		} else {
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "slOff"));
		}
		sendToAll(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		Player p = (Player) sender;
		return commandHandle.execute(p, MessageHandler.getInstance().getLanguage(p), args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> out = new ArrayList<>();
		Player p = (Player) sender;
		commandHandle.tabComplete(p, MessageHandler.getInstance().getLanguage(p), args, out);
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

	public static boolean setStatus(World world, String regionID, boolean on) {
		if (world == null || regionID == null) {
			return false;
		}
		WorldManager.get(world).getPlot(regionID).setSL(on);
		for (Player player : world.getPlayers()) {
			ScoreBoardBau.cmdUpdate(player);
		}
		return true;
	}

	/* easier acess */

	public static boolean setStatus(Location loc, boolean on) {
		String rgID = WorldGuardHandler.getPlotId(loc);
		return setStatus(loc.getWorld(), rgID, on);

	}

	/* get status */

	public static boolean getStatus(World world, String regionID) {
		if (regionID == null || world == null) {
			return false;
		}

		BauWorld bWorld = WorldManager.get(world);
		return bWorld.getPlot(regionID).getSL();
	}

	/* easier acess */

	public static boolean getStatus(Location loc) {
		return getStatus(loc.getWorld(), WorldGuardHandler.getPlotId(loc));
	}

	/* Events for Stoplag */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void pistonextend(BlockPistonExtendEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void pistonRetract(BlockPistonRetractEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void redstone(BlockRedstoneEvent e) {
		if (getStatus(e.getBlock().getLocation())) {
			if (e.getNewCurrent() >= 1 && e.getOldCurrent() < 1) {
				e.setNewCurrent(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFade(BlockFadeEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockForm(BlockFormEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPhysics(BlockPhysicsEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromTo(BlockFromToEvent e) {
		e.setCancelled(getStatus(e.getBlock().getLocation()));
	}

	public void entityPrime(ExplosionPrimeEvent e) {
		e.setCancelled(getStatus(e.getEntity().getLocation()));
	}

}
