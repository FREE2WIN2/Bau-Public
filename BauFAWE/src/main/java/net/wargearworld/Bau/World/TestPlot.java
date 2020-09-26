package net.wargearworld.Bau.World;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.Bau.WorldEdit.Schematic;

public class TestPlot extends Plot{

	protected TestPlot(ProtectedRegion region, String id, Location middleNorth, Schematic ground) {
		super(region, id, middleNorth, ground);
	}


}
