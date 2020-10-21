package net.wargearworld.bau.tools.testBlockSlave.testBlock;

import net.wargearworld.bau.world.plots.Plot;
import org.bukkit.World;

import com.sk89q.worldedit.regions.Region;

import net.wargearworld.bau.worldedit.Schematic;

public class EmptyTestBlock implements ITestBlock {

	private String name;
	private int tier;
	private Region region;
	private Plot plot;
	private Facing facing;
	private World world;
	private Type type;

	public EmptyTestBlock(int tier, Region region, Plot plot, Facing facing, World world,Type type) {
		this.facing = facing;
		this.region = region;
		this.plot = plot;
		this.tier = tier;
		this.setWorld(world);
		this.type = type;
	}

	public EmptyTestBlock() {
	}

	/* GETTER */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Schematic getSchematic() {
		return null;
	}

	@Override
	public int getTier() {
		return tier;
	}

	public Region getRegion() {
		return region;
	}

	public Plot getPlot() {
		return plot;
	}

	public Facing getfacing() {
		return facing;
	}

	public Type getType() {
		return type;
	}
	/* SETTER */
	public void setName(String name) {
		this.name = name;
	}

	public void setTier(int tier) {
		this.tier = tier;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public void setPlot(Plot plot) {
		this.plot = plot;
	}

	public void setFacing(Facing facing) {
		this.facing = facing;
	}

	
	public void setType(Type type) {
		this.type = type;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

}
