package de.AS.Bau.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.sk89q.worldedit.math.BlockVector3;

import de.AS.Bau.Main;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;

public class CoordGetter {

	public static Location getTeleportLocation(World world, String plotID) {
		String[] coords = Main.getPlugin().getConfig().getString("coordinates.teleport." + plotID).split(" ");
		double x = Double.parseDouble(coords[0]);
		double y = Double.parseDouble(coords[1]);
		double z = Double.parseDouble(coords[2]);
		return new Location(world, x, y, z);
	}

	public static BlockVector3 getTBSPastePosition(String plotID, Facing facing) {
		String[] coords = Main.getPlugin().getConfig()
				.getString("coordinates.tbs.paste." + facing.getFace() + "." + plotID).split(" ");
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		return BlockVector3.at(x, y, z);
	}

	public static BlockVector3 getMaxWorldVector() {
		String[] coords = Main.getPlugin().getConfig().getString("coordinates.worldBorder.max").split(" ");
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		return BlockVector3.at(x, y, z);
	}

	public static BlockVector3 getMinWorldVector() {
		String[] coords = Main.getPlugin().getConfig().getString("coordinates.worldBorder.min").split(" ");
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		return BlockVector3.at(x, y, z);
	}

	public static BlockVector3 getMiddleRegionTB(String plotID, Facing facing) {
		String[] coords = Main.getPlugin().getConfig()
				.getString("coordinates.tbs.front." + facing.getFace() + "." + plotID).split(" ");
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		return BlockVector3.at(x, y, z);
	}

	public static BlockVector3 getMaxSizeOfBlock(int tier) {
		ConfigurationSection section = Main.getPlugin().getConfig()
				.getConfigurationSection("coordinates.tbs.sizes." + tier);
		int x = section.getInt("x");
		int y = section.getInt("y");
		int z = section.getInt("z");
		return BlockVector3.at(x, y, z);
	}
}
