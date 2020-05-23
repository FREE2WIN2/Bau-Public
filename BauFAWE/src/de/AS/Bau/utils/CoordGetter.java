package de.AS.Bau.utils;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.math.BlockVector3;

import de.AS.Bau.Main;

public class CoordGetter {

	public static Location getTeleportLocation(World world,String plotID) {
		String[] coords = Main.getPlugin().getConfig().getString("coordinates.teleport."+plotID).split(" ");
		double x = Double.parseDouble(coords[0]);
		double y = Double.parseDouble(coords[1]);
		double z = Double.parseDouble(coords[2]);
		return new Location(world, x, y, z);
	}
	
	public static BlockVector3 getPastePosition(String plotID) {
		String[] coords = Main.getPlugin().getConfig().getString("coordinates.paste."+plotID).split(" ");
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
}
