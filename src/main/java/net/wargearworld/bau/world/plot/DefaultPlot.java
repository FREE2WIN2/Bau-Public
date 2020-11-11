package net.wargearworld.bau.world.plot;

import net.wargearworld.bau.world.bauworld.BauWorld;
import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.worldedit.Schematic;

public class DefaultPlot extends Plot {

	protected DefaultPlot(ProtectedRegion region, String id, Location middleNorth, Schematic ground, BauWorld bauWorld) {
		super(region, id, middleNorth, ground, bauWorld);
	}

	@Override
	public PlotType getType() {
		return PlotType.DEFAULT;
	}


}
