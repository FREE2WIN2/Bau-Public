package net.wargearworld.Bau.World;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.Bau.WorldEdit.Schematic;

public class DefaultPlot extends Plot {

	protected DefaultPlot(ProtectedRegion region, String id, Location middleNorth, Schematic ground) {
		super(region, id, middleNorth, ground);
	}

	

}
