package net.wargearworld.Bau.Tools;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.StringGetterBau;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import net.wargearworld.Bau.WorldEdit.Schematic;
import net.wargearworld.Bau.WorldEdit.WorldEditHandler;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.JsonCreater;
import net.wargearworld.Bau.utils.Scheduler;

public class PlotResetter implements CommandExecutor {
	private static HashSet<UUID> playerBlockedDelete = new HashSet<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		if (sender instanceof Player && args.length == 2) {
			Player p = (Player) sender;
			if (!args[1].equals(p.getUniqueId().toString())) {
				Main.send(p, "error");
				return true;
			}

			if (p.getWorld().getName().equals(p.getUniqueId().toString()) || p.hasPermission("admin")) {
				resetRegion(args[0], p, true);
				return true;
			}
		}
		return false;
	}

	public static void resetRegion(String rgID, Player p, boolean confirmed) {
//		int rgIDint = Integer.parseInt(rgID.replace("plot", ""));
		if (!confirmed) {
			JsonCreater creater1 = new JsonCreater(
					Main.prefix + StringGetterBau.getString(p, "delePlotConfirmation", rgID.replace("plot", "")));
			JsonCreater creater2 = new JsonCreater(
					StringGetterBau.getString(p, "deletePlotHere",rgID.replace("plot", "")));
			creater2.addHoverEvent(StringGetterBau.getString(p, "delePlotHover", rgID.replace("plot", "")));
			creater2.addClickEvent("/delcon " + rgID + " " + p.getUniqueId(), ClickAction.RUN_COMMAND);
			creater1.addJson(creater2).send(p);
		} else {
			if (playerBlockedDelete.contains(p.getUniqueId()) && !p.hasPermission("bau.delete.bypass")) {
				p.sendMessage(StringGetterBau.getString(p, "deletePlotAntiSpaw"));
				return;
			}
			playerBlockedDelete.add(p.getUniqueId());
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

			// spamschutz für nicht VIP und Supporter und builder
			if (!(p.hasPermission("bau.delete.bypass"))) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						playerBlockedDelete.remove(p.getUniqueId());
					}
				}, 20 * 60 * 2);
			}
		}
	}

}
