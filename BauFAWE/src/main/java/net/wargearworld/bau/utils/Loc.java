package net.wargearworld.bau.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class Loc {

	private double x;
	private double y;
	private double z;
	public Loc(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Location toLocation(World world) {
		return new Location(world, x, y, z);
	}
	public static Loc getByString( String string) {
		String[] locs = string.split(" ");
		double x = Double.parseDouble(locs[0]);
		double y = Double.parseDouble(locs[1]);
		double z = Double.parseDouble(locs[2]);
		return new Loc(x, y, z);
	}

}
