package net.wargearworld.bau.world.plot;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.utils.Loc;

public class PlotPattern {

	private Loc middleNorth;
	private Schematic ground;
	private PlotType type;
	private String plotID;

	public PlotPattern(Loc middleNorth, Schematic ground, PlotType type, String plotID) {
		this.middleNorth = middleNorth;
		this.ground = ground;
		this.type = type;
		this.plotID = plotID;
	}

	public Plot toPlot(BauWorld world) {
		/* FactoryMethod for PLot */
		ProtectedRegion region = world.getRegionManager().getRegion(plotID); //TODO exception Handling
		Location middleNorth = this.middleNorth.toLocation(world.getWorld());// middle North
		switch (type) {
		case DEFAULT:
			return new DefaultPlot(region, plotID, middleNorth, ground);
		case TEST:
			return new TestPlot(region, plotID, middleNorth, ground);
		}
		return null;

	}

	public String getID() {
		return plotID;
	}

}
