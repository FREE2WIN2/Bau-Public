package net.wargearworld.bau.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;

public class Stats implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		LocalSession session = wep.getSession(p);
		try {
			Region rg = session.getSelection(BukkitAdapter.adapt(p.getWorld()));
			getStats(rg, p);
		} catch (IncompleteRegionException e) {
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "statsNoRegion"));
			e.printStackTrace();
		}
		return true;
	}

	public void getStats(Region rg, Player p) { // 1.TNT/Alles andere 2.Endstone count 3. TNT
		List<Material> tech = createTechList();
		World world = p.getWorld(); // Count
		Integer tnt_count = 0;
		Integer endstone_count = 0;
		Integer slab_count = 0;
		Integer others_count = 0;
		Integer tech_count = 0;
		Integer air_count = 0;
		BlockVector3 maxPoint = rg.getMaximumPoint();
		BlockVector3 minPoint = rg.getMinimumPoint();
		int minX = minPoint.getBlockX();
		int minY = minPoint.getBlockY();
		int minZ = minPoint.getBlockZ();
		int maxX = maxPoint.getBlockX();
		int maxY = maxPoint.getBlockY();
		int maxZ = maxPoint.getBlockZ();
		if ((maxY - minY) <= 100 && (maxX - minX) <= 100 && (maxZ - minZ) <= 100) {
			for (int newX = minX; newX <= maxX; newX++) {
				for (int newY = minY; newY <= maxY; newY++) {
					for (int newZ = minZ; newZ <= maxZ; newZ++) {
						Location loc = new Location(world, newX, newY, newZ);
						Material m = world.getBlockAt(loc).getType();
						if (m.equals(Material.TNT)) {
							tnt_count = tnt_count + 1;
						} else if (m.equals(Material.END_STONE)) {
							endstone_count = endstone_count + 1;
						} else if (m.toString().contains("SLAB")) {
							slab_count = slab_count + 1;
						} else if (tech.contains(m)) {
							tech_count = tech_count + 1;
						} else if (m.equals(Material.AIR)) {
							air_count = air_count + 1;
						} else {
							others_count = others_count + 1;
						}
					}
				}
			}
			// v - t / v
			int all_count = slab_count + endstone_count + others_count + tnt_count + air_count + tech_count;
			Double percentage = (double) ((((double) all_count - (double) tnt_count) / (double) all_count));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsHeader"));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsEndStone").replace("%r", endstone_count.toString()));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsTNT").replace("%r", tnt_count.toString()));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsTechnic").replace("%r", tech_count.toString()));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsAir").replace("%r", air_count.toString()));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsSlabs").replace("%r", slab_count.toString()));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsRating").replace("%r", percentage.toString()));
			p.sendMessage(MessageHandler.getInstance().getString(p, "statsEnd"));
		}
		else {
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "statsTooBigRegion"));
		}
	}

	public List<Material> createTechList() {
		List<Material> tech = new ArrayList<>();
		// Buttons:
		tech.add(Material.ACACIA_BUTTON);
		tech.add(Material.BIRCH_BUTTON);
		tech.add(Material.DARK_OAK_BUTTON);
		tech.add(Material.JUNGLE_BUTTON);
		tech.add(Material.OAK_BUTTON);
		tech.add(Material.SPRUCE_BUTTON);
		tech.add(Material.STONE_BUTTON);
		// Lever:
		tech.add(Material.LEVER);
		// Pressureplatese:
		tech.add(Material.ACACIA_PRESSURE_PLATE);
		tech.add(Material.BIRCH_PRESSURE_PLATE);
		tech.add(Material.DARK_OAK_PRESSURE_PLATE);
		tech.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		tech.add(Material.JUNGLE_PRESSURE_PLATE);
		tech.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
		tech.add(Material.OAK_PRESSURE_PLATE);
		tech.add(Material.SPRUCE_PRESSURE_PLATE);
		tech.add(Material.STONE_PRESSURE_PLATE);
		// Pistons
		tech.add(Material.PISTON);
		tech.add(Material.PISTON_HEAD);
		tech.add(Material.STICKY_PISTON);
		// Doors
		tech.add(Material.ACACIA_DOOR);
		tech.add(Material.BIRCH_DOOR);
		tech.add(Material.DARK_OAK_DOOR);
		tech.add(Material.IRON_DOOR);
		tech.add(Material.JUNGLE_DOOR);
		tech.add(Material.IRON_DOOR);
		// Trapdoors:
		tech.add(Material.ACACIA_TRAPDOOR);
		tech.add(Material.BIRCH_TRAPDOOR);
		tech.add(Material.DARK_OAK_TRAPDOOR);
		tech.add(Material.IRON_TRAPDOOR);
		tech.add(Material.JUNGLE_TRAPDOOR);
		tech.add(Material.IRON_TRAPDOOR);
		// Redstone
		tech.add(Material.REDSTONE_LAMP);
		tech.add(Material.REDSTONE_WIRE);
		tech.add(Material.REDSTONE_TORCH);
		tech.add(Material.REDSTONE_WALL_TORCH);
		tech.add(Material.REDSTONE_BLOCK);
		tech.add(Material.COMPARATOR);
		tech.add(Material.REPEATER);
		tech.add(Material.OBSERVER);
		tech.add(Material.DROPPER);
		tech.add(Material.HOPPER);
		tech.add(Material.DISPENSER);
		tech.add(Material.LECTERN);

		return tech;
	}

}
