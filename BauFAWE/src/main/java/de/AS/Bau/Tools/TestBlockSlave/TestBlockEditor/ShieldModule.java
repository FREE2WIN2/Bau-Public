package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import de.AS.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import de.AS.Bau.utils.CoordGetter;

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
		switch(type) {
		case ARTILLERY:
			break;
		case ARTOX:
			break;
		case BACKSTAB:
			break;
		case MASSIVE:
			
			Region rg = new CuboidRegion(getMin(plotID, tier, facing), getMax(plotID, tier, facing));
			for(BlockVector3 vector: rg) {
				world.getBlockAt(vecToLoc(vector, world)).setType(Material.END_STONE);
			}
			
			break;
		case SAND:
			break;
		case SPIKE:
			break;
		
		}
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
				return minOfBlock.subtract(1, 0, 0);
			}
		case RIGHTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return minOfBlock.add(blockSize.getX(), 0, 0);
			} else {
				System.out.println(minOfBlock);
				return minOfBlock.subtract(1, 0, -blockSize.divide(2).getZ());
			}
		case LEFTSIDEBACK:
			if (facing == Facing.NORTH) {
				return minOfBlock.subtract(1, 0, -blockSize.divide(2).getZ());
			} else {
				return minOfBlock.add(blockSize.getX() - 1, 0, 0);
			}
		case LEFTSIDEFRONT:
			if (facing == Facing.NORTH) {
				return minOfBlock.subtract(1, 0, 0);
			} else {
				return minOfBlock.add(blockSize.getX() - 1, 0, blockSize.divide(2).getZ());
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
				return maxOfBlock.subtract(blockSize.getX() - 1, 0, 0);
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
}
