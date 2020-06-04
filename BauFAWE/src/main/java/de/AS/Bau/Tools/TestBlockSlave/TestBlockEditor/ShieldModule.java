package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import org.bukkit.World;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import de.AS.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import de.AS.Bau.utils.CoordGetter;

public class ShieldModule {

	ShieldType type;
	int tier;
	ShieldPosition position;
	Facing facing;

	public ShieldModule(ShieldType type, int tier, ShieldPosition position, Facing facing) {
		this.type = type;
		this.tier = tier;
		this.position = position;
		this.facing = facing;
	}

	public ShieldPosition getPosition() {
		return position;
	}

	public ShieldType getType() {
		return type;
	}
	
	public void visualize(String plotID, World world) {
		
	}
	
	public BlockVector3 getMin(String plotID) {
		Region blockRegion = TestBlockSlaveCore.getTBRegion(tier, plotID, facing);
		BlockVector3 minOfBlock = blockRegion.getMinimumPoint();
		BlockVector3 blockSize = CoordGetter.getMaxSizeOfBlock(tier);
		int shieldSizes = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
		switch(position) {
		case BACK:
			if(facing == Facing.NORTH) {
				return minOfBlock.add(0,0,blockSize.getZ());
			}else {
				return minOfBlock.subtract(0,0,shieldSizes);
			}
		case FRONT:
			if(facing == Facing.NORTH) {
				return minOfBlock.subtract(0,0,shieldSizes);
			}else {
				return minOfBlock.add(0,0,blockSize.getZ());
			}
		case ROOFBACK:
			if(facing == Facing.NORTH) {
				return minOfBlock.add(0,blockSize.getY(),blockSize.divide(2).getZ());
			}else {
				return minOfBlock.add(0,blockSize.getY(),0);
			}
		case ROOFFRONT:
			if(facing == Facing.NORTH) {
				return minOfBlock.subtract(0,-blockSize.getY(),0);
			}else {
				return minOfBlock.add(0,blockSize.getY(),blockSize.divide(2).getZ());
			}
		case RIGHTSIDEBACK:
			if(facing == Facing.NORTH) {
				return minOfBlock.add(blockSize.getX(),0,blockSize.divide(2).getZ());
			}else {
				return minOfBlock.subtract(1,0,0);
			}
		case RIGHTSIDEFRONT:
			if(facing == Facing.NORTH) {
				return minOfBlock.add(blockSize.getX(),0,0);
			}else {
				System.out.println(minOfBlock);
				return minOfBlock.subtract(1,0,-blockSize.divide(2).getZ());
			}
		case LEFTSIDEBACK:
			if(facing == Facing.NORTH) {
				return minOfBlock.subtract(1,0,-blockSize.divide(2).getZ());
			}else {
				return minOfBlock.add(blockSize.getX()-1,0,0);
			}
		case LEFTSIDEFRONT:
			if(facing == Facing.NORTH) {
				return minOfBlock.subtract(1,0,0);
			}else {
				return minOfBlock.add(blockSize.getX()-1,0,blockSize.divide(2).getZ());
			}
		}
		return null;
	}

	public BlockVector3 getMax(String plotID) {
		Region blockRegion = TestBlockSlaveCore.getTBRegion(tier, plotID, facing);
		BlockVector3 maxOfBlock = blockRegion.getMaximumPoint();
		BlockVector3 blockSize = CoordGetter.getMaxSizeOfBlock(tier);
		int shieldSizes = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
		switch(position) {
		case BACK:
			if(facing == Facing.NORTH) {
				return maxOfBlock.add(0,0,blockSize.getZ());
			}else {
				return maxOfBlock.subtract(0,0,shieldSizes);
			}
		case FRONT:
			if(facing == Facing.NORTH) {
				return maxOfBlock.subtract(0,0,shieldSizes);
			}else {
				return maxOfBlock.add(0,0,blockSize.getZ());
			}
		case ROOFBACK:
			if(facing == Facing.NORTH) {
				return maxOfBlock.add(0,blockSize.getY(),blockSize.divide(2).getZ());
			}else {
				return maxOfBlock.add(0,blockSize.getY(),0);
			}
		case ROOFFRONT:
			if(facing == Facing.NORTH) {
				return maxOfBlock.subtract(0,-blockSize.getY(),0);
			}else {
				return maxOfBlock.add(0,blockSize.getY(),blockSize.divide(2).getZ());
			}
		case RIGHTSIDEBACK:
			if(facing == Facing.NORTH) {
				return maxOfBlock.add(blockSize.getX(),0,blockSize.divide(2).getZ());
			}else {
				return maxOfBlock.subtract(1,0,0);
			}
		case RIGHTSIDEFRONT:
			if(facing == Facing.NORTH) {
				return maxOfBlock.add(blockSize.getX(),0,0);
			}else {
				System.out.println(maxOfBlock);
				return maxOfBlock.subtract(1,0,-blockSize.divide(2).getZ());
			}
		case LEFTSIDEBACK:
			if(facing == Facing.NORTH) {
				return maxOfBlock.subtract(1,0,-blockSize.divide(2).getZ());
			}else {
				return maxOfBlock.add(blockSize.getX()-1,0,0);
			}
		case LEFTSIDEFRONT:
			if(facing == Facing.NORTH) {
				return maxOfBlock.subtract(1,0,0);
			}else {
				return maxOfBlock.add(blockSize.getX()-1,0,blockSize.divide(2).getZ());
			}
		}
		return null;
	}
}
