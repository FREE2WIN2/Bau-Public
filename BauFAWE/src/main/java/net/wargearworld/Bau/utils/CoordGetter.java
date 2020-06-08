package net.wargearworld.Bau.utils;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldedit.math.BlockVector3;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.Plots.Plots;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;

public class CoordGetter {

	public static Location getTeleportLocation(World world, String plotID) {
		YamlConfiguration config = getConfigOfWorld(world.getName());
		String[] coords = config.getString("coordinates.teleport." + plotID).split(" ");
		double x = Double.parseDouble(coords[0]);
		double y = Double.parseDouble(coords[1]);
		double z = Double.parseDouble(coords[2]);
		return new Location(world, x, y, z);
	}

	public static BlockVector3 getTBSPastePosition(String plotID, Facing facing, String worldName) {
		YamlConfiguration config = getConfigOfWorld(worldName);
		String[] coords = config.getString("coordinates.tbs.paste." + facing.getFace() + "." + plotID).split(" ");
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		return BlockVector3.at(x, y, z);
	}

	public static YamlConfiguration getConfigOfWorld(String worldName) {
		if (worldName.contains("test")) {
			return Plots.getConfig(Main.getPlugin().getCustomConfig().getString("test.template"));
		}
		return Plots.getConfigOfPlot(UUID.fromString(worldName));
	}

	public static BlockVector3 getMiddleRegionTB(String plotID, Facing facing, String worldName) {
		YamlConfiguration config = getConfigOfWorld(worldName);
		String[] coords = config.getString("coordinates.tbs.front." + facing.getFace() + "." + plotID).split(" ");
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		return BlockVector3.at(x, y, z);
	}

	public static BlockVector3 getMaxSizeOfBlock(int tier) {
		ConfigurationSection section = Main.getPlugin().getConfig()
				.getConfigurationSection("sizes." + tier);
		int x = section.getInt("x");
		int y = section.getInt("y");
		int z = section.getInt("z");
		return BlockVector3.at(x, y, z);
	}

	public static BlockVector3 locToVec(Location loc) {
		return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
	}

}
