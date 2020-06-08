package net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor.Iterators.FrontIterator;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor.Iterators.RoofIterator;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor.Iterators.SideIterator;
import net.wargearworld.Bau.utils.CoordGetter;

public class ShieldModule {

	private ShieldType type;
	private ShieldPosition position;

	public ShieldModule(ShieldType type, ShieldPosition position) {
		this.type = type;
		this.position = position;
	}

	public ShieldPosition getPosition() {
		return position;
	}

	public ShieldType getType() {
		return type;
	}

	public void visualize(String plotID, World world, int tier, Facing facing) {
		Region rg = null;
		BlockVector3 min;
		BlockVector3 max;
		switch (type) {
		case ARTILLERY:
			/* Get Front */
			rg = getFrontRegion(plotID, tier, facing,world.getName());
			setRegionToMaterial(world, rg, Material.END_STONE);

			/* get Upper region */
			min = rg.getMinimumPoint();
			max = rg.getMaximumPoint();
			Region up = new CuboidRegion(BlockVector3.at(min.getX(), max.getY(), min.getZ()), max);
			setRegionToMaterial(world, up, Material.SANDSTONE_WALL);
			break;
		case ARTOX:
			try {
				showArtox(plotID, tier, facing, world);
			} catch (Exception e) {
			}
			break;
		case BACKSTAB:
			rg = getUpperRegion(plotID, tier, facing,world.getName());
			setRegionToMaterial(world, rg, Material.END_STONE);
			if (position != ShieldPosition.ROOFBACK && position != ShieldPosition.ROOFFRONT) {
				rg = getMiddleRegion(plotID, tier, facing,world.getName());
				setRegionToMaterial(world, rg, Material.END_STONE);
			}
			break;
		case MASSIVE:
			rg = new CuboidRegion(getMin(plotID, tier, facing,world.getName()), getMax(plotID, tier, facing,world.getName()));
			setRegionToMaterial(world, rg, Material.END_STONE);

			break;
		case SAND:
			rg = getFrontRegion(plotID, tier, facing,world.getName());
			setRegionToMaterial(world, rg, Material.SAND);
			break;
		case SPIKE:
			rg = new CuboidRegion(getMin(plotID, tier, facing,world.getName()), getMax(plotID, tier, facing,world.getName()));
			setSpikes(world, rg, Material.END_STONE);
			break;

		}
	}

	private void showArtox(String plotID, int tier, Facing facing, World world) throws RegionOperationException {
		int shiftz = 0;
		int shifty = 0;
		int shiftx = 0;
		BlockFace blockFace = null;
		Region rg = null;
		switch (position) {
		case BACK:
			rg = getFrontRegion(plotID, tier, facing,world.getName());

			if (facing == Facing.SOUTH) {
				blockFace = BlockFace.NORTH;
				shiftz = 1;
			} else {
				blockFace = BlockFace.SOUTH;
				shiftz = -1;
			}
			break;
		case FRONT:
			rg = getFrontRegion(plotID, tier, facing,world.getName());
			if (facing == Facing.SOUTH) {
				blockFace = BlockFace.SOUTH;
				shiftz = -1;
			} else {
				blockFace = BlockFace.NORTH;
				shiftz = 1;
			}
			break;
		case LEFTSIDEBACK:
		case LEFTSIDEFRONT:
			rg = getSideRegion(plotID, tier, facing,world.getName());
			if (facing == Facing.SOUTH) {
				blockFace = BlockFace.EAST;
				shiftx = -1;
			} else {
				blockFace = BlockFace.WEST;
				shiftx = 1;
			}
			break;
		case RIGHTSIDEBACK:
		case RIGHTSIDEFRONT:
			rg = getSideRegion(plotID, tier, facing,world.getName());
			if (facing == Facing.SOUTH) {
				blockFace = BlockFace.WEST;
				shiftx = 1;
			} else {
				blockFace = BlockFace.EAST;
				shiftx = -1;
			}
			break;
		case ROOFBACK:
		case ROOFFRONT:
			blockFace = BlockFace.UP;
			rg = getUpperRegion(plotID, tier, facing,world.getName());
			shifty = -1;
			break;
		}
		setRegionToMaterial(world, rg, Material.END_STONE);

		rg.shift(BlockVector3.at(shiftx, shifty, shiftz));
		Directional bd = (Directional) Material.PISTON.createBlockData();
		bd.setFacing(blockFace);
		setRegionToBlockData(world, rg, bd);

		rg.shift(BlockVector3.at(shiftx, shifty, shiftz));
		bd = (Directional) Material.OBSERVER.createBlockData();
		bd.setFacing(blockFace);
		setRegionToBlockData(world, rg, bd);

	}

	private Region getMiddleRegion(String plotID, int tier, Facing facing,String worldName) {
		BlockVector3 min = getMin(plotID, tier, facing,worldName);
		BlockVector3 max = getMax(plotID, tier, facing,worldName);
		int half = (max.getY() - min.getY()) / 2;
		min = min.add(0, half, 0);
		max = max.subtract(0, half, 0);
		if (min.getY() != max.getY()) {
			min = min.add(0, 1, 0);
		}
		return new CuboidRegion(min, max);
	}

	public Region getUpperRegion(String plotID, int tier, Facing facing,String worldName) {
		BlockVector3 min = getMin(plotID, tier, facing,worldName);
		BlockVector3 max = getMax(plotID, tier, facing,worldName);
		min = BlockVector3.at(min.getX(), max.getY(), min.getZ());
		return new CuboidRegion(min, max);
	}

	public Region getFrontRegion(String plotID, int tier, Facing facing,String worldName) {
		BlockVector3 min = getMin(plotID, tier, facing,worldName);
		BlockVector3 max = getMax(plotID, tier, facing,worldName);
		switch (position) {
		case BACK:
			switch (facing) {
			case NORTH:
				min = BlockVector3.at(min.getX(), min.getY(), max.getZ());
				break;
			case SOUTH:
				max = BlockVector3.at(max.getX(), max.getY(), min.getZ());
				break;
			}
			return new CuboidRegion(min, max);

		case LEFTSIDEBACK:
		case LEFTSIDEFRONT:
			if (type == ShieldType.SAND) {
				return getSideRegion(plotID, tier, facing,worldName);
			}
		case RIGHTSIDEBACK:
		case RIGHTSIDEFRONT:
			if (type == ShieldType.SAND) {
				return getSideRegion(plotID, tier, facing,worldName);
			}
		case FRONT:
			if (type == ShieldType.ARTILLERY) {
				int shieldSize = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
				max = max.add(0, shieldSize, 0);
			}
			switch (facing) {
			case NORTH:
				max = BlockVector3.at(max.getX(), max.getY(), min.getZ());
				break;
			case SOUTH:
				min = BlockVector3.at(min.getX(), min.getY(), max.getZ());
				break;
			}
			return new CuboidRegion(min, max);

		case ROOFBACK:
		case ROOFFRONT:

			switch (facing) {
			case NORTH:
				max = BlockVector3.at(max.getX(), max.getY(), min.getZ());
				break;
			case SOUTH:
				min = BlockVector3.at(min.getX(), min.getY(), max.getZ());
				break;
			}
			return new CuboidRegion(min, max);

		}
		return null;
	}

	public Region getSideRegion(String plotID, int tier, Facing facing,String worldName) {
		BlockVector3 min = getMin(plotID, tier, facing,worldName);
		BlockVector3 max = getMax(plotID, tier, facing,worldName);
		switch (position) {
		case LEFTSIDEBACK:
		case LEFTSIDEFRONT:

			switch (facing) {
			case NORTH:
				max = BlockVector3.at(min.getX(), max.getY(), max.getZ());
				break;
			case SOUTH:
				min = BlockVector3.at(max.getX(), min.getY(), min.getZ());
				break;
			}
			return new CuboidRegion(min, max);
		case RIGHTSIDEBACK:
		case RIGHTSIDEFRONT:
			switch (facing) {
			case NORTH:
				// xgrößer
				min = BlockVector3.at(max.getX(), min.getY(), min.getZ());
				break;
			case SOUTH:
				// xkleiner
				max = BlockVector3.at(min.getX(), max.getY(), max.getZ());
				break;
			}
			return new CuboidRegion(min, max);

		default:
			break;
		}
		return null;
	}

	public Location vecToLoc(BlockVector3 vector, World world) {
		return new Location(world, vector.getX(), vector.getY(), vector.getZ());
	}

	public BlockVector3 getMin(String plotID, int tier, Facing facing,String worldName) {
		Region blockRegion = TestBlockSlaveCore.getTBRegion(tier, plotID, facing,worldName);
		BlockVector3 minOfBlock = blockRegion.getMinimumPoint();
		BlockVector3 blockSize = CoordGetter.getMaxSizeOfBlock(tier);
		int shieldSizes = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
		switch (position) {
		case BACK:
			if (facing == Facing.NORTH) {
				return minOfBlock.add(0, 0, blockSize.getZ());
			} else {
				return minOfBlock.subtract(0, 0, shieldSizes);
			}
		case FRONT:
			if (facing == Facing.NORTH) {
				return minOfBlock.subtract(0, 0, shieldSizes);
			} else {
				return minOfBlock.add(0, 0, blockSize.getZ());
			}
		case ROOFBACK:
			if (facing == Facing.NORTH) {
				return minOfBlock.add(0, blockSize.getY(), blockSize.divide(2).getZ());
			} else {
				return minOfBlock.add(0, blockSize.getY(), 0);
			}
		case ROOFFRONT:
			if (facing == Facing.NORTH) {
				return minOfBlock.subtract(0, -blockSize.getY(), 0);
			} else {
				return minOfBlock.add(0, blockSize.getY(), blockSize.divide(2).getZ());
			}
		case RIGHTSIDEBACK:
			if (facing == Facing.NORTH) {
				return minOfBlock.add(blockSize.getX(), 0, blockSize.divide(2).getZ());
			} else {
				return minOfBlock.subtract(shieldSizes, 0, 0);
			}
		case RIGHTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return minOfBlock.add(blockSize.getX(), 0, 0);
			} else {
				return minOfBlock.subtract(shieldSizes, 0, -blockSize.divide(2).getZ());
			}
		case LEFTSIDEBACK:
			if (facing == Facing.NORTH) {
				return minOfBlock.subtract(shieldSizes, 0, -blockSize.divide(2).getZ());
			} else {
				return minOfBlock.add(blockSize.getX(), 0, 0);
			}
		case LEFTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return minOfBlock.subtract(shieldSizes, 0, 0);
			} else {
				return minOfBlock.add(blockSize.getX(), 0, blockSize.divide(2).getZ());
			}
		}
		return null;
	}

	public BlockVector3 getMax(String plotID, int tier, Facing facing,String worldName) {
		Region blockRegion = TestBlockSlaveCore.getTBRegion(tier, plotID, facing,worldName);
		BlockVector3 maxOfBlock = blockRegion.getMaximumPoint();
		BlockVector3 blockSize = CoordGetter.getMaxSizeOfBlock(tier);
		int shieldSizes = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
		switch (position) {
		case BACK:
			if (facing == Facing.NORTH) {
				return maxOfBlock.add(0, 0, shieldSizes);
			} else {
				return maxOfBlock.subtract(0, 0, blockSize.getZ());
			}
		case FRONT:
			if (facing == Facing.NORTH) {
				return maxOfBlock.subtract(0, 0, blockSize.getZ());
			} else {
				return maxOfBlock.add(0, 0, shieldSizes);
			}
		case ROOFBACK:
			if (facing == Facing.NORTH) {
				return maxOfBlock.subtract(0, -shieldSizes, 0);
			} else {
				return maxOfBlock.subtract(0, -shieldSizes, blockSize.divide(2).getZ() - 1);
			}
		case ROOFFRONT:
			if (facing == Facing.NORTH) {
				return maxOfBlock.subtract(0, -shieldSizes, blockSize.divide(2).getZ());
			} else {
				return maxOfBlock.add(0, shieldSizes, 0);
			}
		case RIGHTSIDEBACK:
			if (facing == Facing.NORTH) {
				return maxOfBlock.add(shieldSizes, 0, 0);
			} else {
				return maxOfBlock.subtract(blockSize.getX(), 0, blockSize.divide(2).getZ() - 1);
			}
		case RIGHTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return maxOfBlock.add(shieldSizes, 0, -blockSize.divide(2).getZ());
			} else {
				return maxOfBlock.subtract(blockSize.getX(), 0, 0);
			}
		case LEFTSIDEBACK:
			if (facing == Facing.NORTH) {
				return maxOfBlock.subtract(blockSize.getX(), 0, 0);
			} else {
				if(tier ==2) {
					return maxOfBlock.add(shieldSizes, 0, -blockSize.divide(2).getZ());					
				}
				return maxOfBlock.add(shieldSizes, 0, -blockSize.divide(2).getZ() - 1);
			}
		case LEFTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return maxOfBlock.subtract(blockSize.getX(), 0, blockSize.divide(2).getZ());
			} else {
				return maxOfBlock.add(shieldSizes, 0, 0);
			}
		}
		return null;
	}

	private void setRegionToMaterial(World world, Region rg, Material mat) {
		for (BlockVector3 vector : rg) {
			world.getBlockAt(vecToLoc(vector, world)).setType(mat, true);
		}
	}

	private void setRegionToBlockData(World world, Region rg, BlockData data) {
		for (BlockVector3 vector : rg) {
			world.getBlockAt(vecToLoc(vector, world)).setBlockData(data, true);
		}
	}

	private void setSpikes(World world, Region rg, Material mat) {

		Iterator<Region> iterator;
		switch (position) {
		case BACK:
		case FRONT:
			iterator = new FrontIterator(rg);
			break;
		case ROOFBACK:
		case ROOFFRONT:
			iterator = new RoofIterator(rg);
			break;
		default:
			iterator = new SideIterator(rg);
			break;
		}
		while (iterator.hasNext()) {
			setRegionToMaterial(world, iterator.next(), mat);
		}

	}
}
