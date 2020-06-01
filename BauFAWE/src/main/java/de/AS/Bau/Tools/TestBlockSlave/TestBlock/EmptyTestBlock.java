package de.AS.Bau.Tools.TestBlockSlave.TestBlock;

import org.bukkit.World;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

import de.AS.Bau.Main;
import de.AS.Bau.WorldEdit.Schematic;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.Facing;

public class EmptyTestBlock implements TestBlock {

	private String name;
	private int tier;
	private Region region;
	private String plotID;
	private Facing facing;
	private World world;

	public EmptyTestBlock(int tier, Region region, String regionID, Facing facing, World world) {
		this.facing = facing;
		this.region = region;
		this.plotID = regionID;
		this.tier = tier;
		this.world = world;
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
}
