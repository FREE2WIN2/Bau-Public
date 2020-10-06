package net.wargearworld.Bau.Tools;

import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.InvisibleNode.invisible;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import net.wargearworld.Bau.WorldEdit.Schematic;
import net.wargearworld.Bau.WorldEdit.WorldEditHandler;
import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.JsonCreater;
import net.wargearworld.Bau.utils.Scheduler;
import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.Arguments.StringArgument;

public class PlotResetter implements TabExecutor {

	private CommandHandel commandHandle;

	public PlotResetter() {
//		new CommandManager(MessageHandler.getInstance());
		commandHandle = new CommandHandel("plotreset", Main.prefix,Main.getPlugin());
		commandHandle.setCallback(s -> {
			String rgID = WorldGuardHandler.getPlotId(s.getPlayer().getLocation());
			resetRegion(rgID, s.getPlayer(), false);
		});

		commandHandle.addSubNode(invisible(argument("plotID", new StringArgument())
				.addSubNode(argument("UUID", new StringArgument()).setCallback(s -> {
					resetRegion(s.getString(s.getString("plotID")), s.getPlayer(), true);
				}).setRequirement(s -> {
					return s.getPlayer().getUniqueId().toString().equals(s.getString("UUID"));
				}))));

		commandHandle.addSubNode(literal("undo").setCallback(s -> {
			undo(s);
		}));

	}

	private void undo(ArgumentList s) {
		Player p = s.getPlayer();
		BauPlayer player = BauPlayer.getBauPlayer(p);
		if (player.getCurrentPlot().undo(p.getWorld())) {
			// TODO msg
		} else {
			// TODO msg
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			return commandHandle.execute(p, MessageHandler.getInstance().getLanguage(p), args);
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command arg1,
			String arg2, String[] args) {
		List<String> out = new ArrayList<>();
		Player p = (Player) sender;
		commandHandle.tabComplete(p, MessageHandler.getInstance().getLanguage(p), args,out);
		return out;
	}

	public static void resetRegion(String rgID, Player p, boolean confirmed) {
//		int rgIDint = Integer.parseInt(rgID.replace("plot", ""));
		if (!confirmed) {
			JsonCreater creater1 = new JsonCreater(Main.prefix
					+ MessageHandler.getInstance().getString(p, "delePlotConfirmation", rgID.replace("plot", "")));
			JsonCreater creater2 = new JsonCreater(
					MessageHandler.getInstance().getString(p, "deletePlotHere", rgID.replace("plot", "")));
			creater2.addHoverEvent(
					MessageHandler.getInstance().getString(p, "delePlotHover", rgID.replace("plot", "")));
			creater2.addClickEvent("/plotreset " + rgID + " " + p.getUniqueId(), ClickAction.RUN_COMMAND);
			creater1.addJson(creater2).send(p);
		} else {
//			if (playerBlockedDelete.contains(p.getUniqueId()) && !p.hasPermission("bau.delete.bypass")) {
//				p.sendMessage(MessageHandler.getInstance().getString(p, "deletePlotAntiSpaw"));
//				return;
//			}
//			playerBlockedDelete.add(p.getUniqueId());
			Main.send(p, "delePlot", rgID.replace("plot", ""));
			// für jede Zeile rgid festlegen
			ProtectedRegion rg = WorldGuard.getInstance().getPlatform().getRegionContainer()
					.get(BukkitAdapter.adapt(p.getWorld())).getRegion(rgID);

			int xmin = rg.getMinimumPoint().getBlockX();
			int xmax = rg.getMaximumPoint().getBlockX();
			int ymax = rg.getMaximumPoint().getBlockY();// ganz oben
			int ymin = rg.getMinimumPoint().getBlockY();// ganz unten
			int zmin = rg.getMinimumPoint().getBlockZ();
			int zmax = rg.getMaximumPoint().getBlockZ();

			Scheduler scheduler = new Scheduler();
			scheduler.setX(xmin);
			scheduler.setY(ymin);
			scheduler.setZ(zmin);

			World world = p.getWorld();
			// paste
			String schemName = CoordGetter.getConfigOfWorld(p.getWorld().getName())
					.getString("plotreset.schemfiles." + rgID) + ".schem";
			WorldEditHandler.pasteground(new Schematic("TestBlockSklave", schemName, Facing.NORTH), rgID, p, true);

			int maxBlockChangePerTick = WorldEditHandler.maxBlockChangePerTick;
			scheduler.setTask(
					Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

						@Override
						public void run() {
							int xmins = scheduler.getX();
							int ymins = scheduler.getY();
							int zmins = scheduler.getZ();
							int blockcount = 0;
							for (int x = xmins; x <= xmax; x++) {
								for (int y = ymins; y <= ymax; y++) {
									for (int z = zmins; z <= zmax; z++) {
										Block b = world.getBlockAt(x, y, z);
										blockcount++;
										if (!b.getType().equals(Material.AIR)) {
											b.setType(Material.AIR, false);
										}
										if (blockcount == maxBlockChangePerTick) {
											scheduler.setX(x);
											scheduler.setY(y);
											scheduler.setZ(z);
											return;
										}
									}
									zmins = zmin;
								}
								ymins = ymin;
							}
							scheduler.cancel();
						}
					}, 0, 1));
		}
	}

	

}
