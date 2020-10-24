package net.wargearworld.bau.world.plots;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.worldedit.Schematic;

public class DefaultPlot extends Plot {

	protected DefaultPlot(ProtectedRegion region, String id, Location middleNorth, Schematic ground) {
		super(region, id, middleNorth, ground);
	}

	@Override
	public PlotType getType() {
		return PlotType.DEFAULT;
	}


}
