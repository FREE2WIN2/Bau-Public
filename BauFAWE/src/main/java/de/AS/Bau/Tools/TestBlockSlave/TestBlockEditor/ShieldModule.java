package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

import de.AS.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import de.AS.Bau.utils.CoordGetter;
import net.minecraft.server.v1_15_R1.BlockPosition;

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
		Region rg;
		BlockVector3 min;
		BlockVector3 max;
		switch (type) {
		case ARTILLERY:
			/* Get Front */
			rg = getFrontRegion(plotID, tier, facing);
			setRegionToMaterial(world, rg, Material.END_STONE);

			/* get Upper region */
			min = rg.getMinimumPoint();
			max = rg.getMaximumPoint();
			Region up = new CuboidRegion(BlockVector3.at(min.getX(), max.getY(), min.getZ()), max);
			setRegionToMaterial(world, up, Material.SANDSTONE_WALL);
			updatePhysics(world, up);
			break;
		case ARTOX:
			break;
		case BACKSTAB:
			break;
		case MASSIVE:
			rg = new CuboidRegion(getMin(plotID, tier, facing), getMax(plotID, tier, facing));
			setRegionToMaterial(world, rg, Material.END_STONE);

			break;
		case SAND:
			rg = getFrontRegion(plotID, tier, facing);
			setRegionToMaterial(world, rg, Material.SAND);
			break;
		case SPIKE:
			break;

		}
	}

	private void updatePhysics(World world, Region up) {
		BlockVector3 middle = up.getCenter().toBlockPoint();
		Block block = world.getBlockAt(middle.getX(), middle.getY(), middle.getZ());
		((CraftWorld) world).getHandle().applyPhysics(new BlockPosition(block.getX(), block.getY(), block.getZ()),
				((CraftBlock) block).getNMS().getBlock());

	}

	public Region getFrontRegion(String plotID, int tier, Facing facing) {
		BlockVector3 min = getMin(plotID, tier, facing);
		BlockVector3 max = getMax(plotID, tier, facing);
		System.out.println("origin: min= " + min + " max=" + max);
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
			switch (facing) {
			case NORTH:
				// xkleiner
				max = BlockVector3.at(min.getX(), max.getY(), max.getZ());
				break;
			case SOUTH:
				// x größer
				min = BlockVector3.at(max.getX(), min.getY(), min.getZ());
				break;
			}
			System.out.println("Min="+min + " max:" + max);
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
			System.out.println(min + " " + max);
			return new CuboidRegion(min, max);

		case FRONT:
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

	public Location vecToLoc(BlockVector3 vector, World world) {
		return new Location(world, vector.getX(), vector.getY(), vector.getZ());
	}

	public BlockVector3 getMin(String plotID, int tier, Facing facing) {
		Region blockRegion = TestBlockSlaveCore.getTBRegion(tier, plotID, facing);
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

	public BlockVector3 getMax(String plotID, int tier, Facing facing) {
		Region blockRegion = TestBlockSlaveCore.getTBRegion(tier, plotID, facing);
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
				return maxOfBlock.subtract(0, -shieldSizes, blockSize.divide(2).getZ());
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
				return maxOfBlock.subtract(blockSize.getX(), 0, blockSize.divide(2).getZ());
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
				return maxOfBlock.add(shieldSizes, 0, -blockSize.divide(2).getZ());
			}
		case LEFTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return maxOfBlock.subtract(blockSize.getX() + shieldSizes, 0, blockSize.divide(2).getZ());
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

	}
}
