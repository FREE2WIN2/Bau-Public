package net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock;

import org.bukkit.World;

import com.sk89q.worldedit.regions.Region;

import net.wargearworld.Bau.WorldEdit.Schematic;

public class EmptyTestBlock implements TestBlock {

	private String name;
	private int tier;
	private Region region;
	private String plotID;
	private Facing facing;
	private World world;
	private Type type;

	public EmptyTestBlock(int tier, Region region, String regionID, Facing facing, World world,Type type) {
		this.facing = facing;
		this.region = region;
		this.plotID = regionID;
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

	public String getPlotID() {
		return plotID;
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

	public void setPlotID(String plotID) {
		this.plotID = plotID;
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
