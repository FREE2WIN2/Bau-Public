package de.AS.Bau.Tools;

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

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import de.AS.Bau.WorldEdit.Schematic;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.utils.ClickAction;
import de.AS.Bau.utils.JsonCreater;
import de.AS.Bau.utils.Scheduler;

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

			if (p.getWorld().getName().equals(p.getUniqueId().toString())|| p.hasPermission("admin")) {
				resetRegion(args[0], p, true);
				return true;
			}
		}
		return false;
	}

	public static void resetRegion(String rgID, Player p, boolean confirmed) {
		int rgIDint = Integer.parseInt(rgID.replace("plot", ""));
		if (!confirmed) {
			JsonCreater creater1 = new JsonCreater(
					Main.prefix + StringGetterBau.getString(p, "delePlotConfirmation").replace("%r", "" + rgIDint));
			JsonCreater creater2 = new JsonCreater(
					StringGetterBau.getString(p, "deletePlotHere").replace("%r", "" + rgIDint));
			creater2.addHoverEvent(StringGetterBau.getString(p, "delePlotHover").replace("%r", "" + rgIDint));
			creater2.addClickEvent("/delcon " + rgID + " " + p.getUniqueId(), ClickAction.RUN_COMMAND);
			creater1.addJson(creater2).send(p);
		} else {
			if (playerBlockedDelete.contains(p.getUniqueId()) && !p.hasPermission("bau.delete.bypass")) {
				p.sendMessage(StringGetterBau.getString(p, "deletePlotAntiSpaw"));
				return;
			}
			playerBlockedDelete.add(p.getUniqueId());
			p.sendMessage(StringGetterBau.getString(p, "delePlot").replace("%r", "" + rgIDint));
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
			WorldEditHandler.pasten(new Schematic("TestBlockSklave", "ground.schem", Facing.NORTH), rgID, p, true);

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
