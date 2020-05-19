package de.AS.Bau.WorldEdit;

import java.util.List;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
/**
 * Used with WE Code, edited to save BLockState and not BaseBlocks
 * 
 * 
 * 
 */
public class BlockStateClipboard implements Clipboard {

	private final Region region;
	private BlockVector3 origin;
	private final BlockState[][][] blocks;
	private BiomeType[][] biomes = null;
	
	public BlockStateClipboard(Region rg) {
		this.region = rg.clone();
		this.origin = region.getMinimumPoint();

		BlockVector3 dimensions = getDimensions();
		blocks = new BlockState[dimensions.getBlockX()][dimensions.getBlockY()][dimensions.getBlockZ()];
	}
	
	
	@Override
	public Entity createEntity(Location arg0, BaseEntity arg1) {
		//we dont need
		return null;
	}

	@Override
	public List<? extends Entity> getEntities() {
		//we dont need
		return null;
	}

	@Override
	public List<? extends Entity> getEntities(Region arg0) {
		//we dont need
		return null;
	}

	@Override
	public BlockVector3 getMaximumPoint() {
		return region.getMaximumPoint();
	}

	@Override
	public BlockVector3 getMinimumPoint() {
		return region.getMinimumPoint();
	}

	@Override
	public BiomeType getBiome(BlockVector2 position) {
        if (biomes != null
                && position.containedWithin(getMinimumPoint().toBlockVector2(), getMaximumPoint().toBlockVector2())) {
            BlockVector2 v = position.subtract(region.getMinimumPoint().toBlockVector2());
            BiomeType biomeType = biomes[v.getBlockX()][v.getBlockZ()];
            if (biomeType != null) {
                return biomeType;
            }
        }

        return BiomeTypes.OCEAN;
    }

	    @Override
    public BlockState getBlock(BlockVector3 position) {
        if (region.contains(position)) {
            BlockVector3 v = position.subtract(region.getMinimumPoint());
            BlockState block = blocks[v.getBlockX()][v.getBlockY()][v.getBlockZ()];
            if (block != null) {
                return block.toImmutableState();
            }
        }

        return BlockTypes.AIR.getDefaultState();
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        if (region.contains(position)) {
            BlockVector3 v = position.subtract(region.getMinimumPoint());
            BaseBlock block = blocks[v.getBlockX()][v.getBlockY()][v.getBlockZ()].toBaseBlock();
            if (block != null) {
                return block;
            }
        }

        return BlockTypes.AIR.getDefaultState().toBaseBlock();
    }
	@Override
	public Operation commit() {
		return null;
	}

	@Override
	public boolean setBiome(BlockVector2 position, BiomeType biome) {
		  if (position.containedWithin(getMinimumPoint().toBlockVector2(), getMaximumPoint().toBlockVector2())) {
	            BlockVector2 v = position.subtract(region.getMinimumPoint().toBlockVector2());
	            if (biomes == null) {
	                biomes = new BiomeType[region.getWidth()][region.getLength()];
	            }
	            biomes[v.getBlockX()][v.getBlockZ()] = biome;
	            return true;
	        }
	        return false;
	}

	
	public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, BlockState block) throws WorldEditException {
		 if (region.contains(position)) {
	            BlockVector3 v = position.subtract(region.getMinimumPoint());
	            blocks[v.getBlockX()][v.getBlockY()][v.getBlockZ()] = block;
	            return true;
	        } else {
	            return false;
	        }
	}

	@Override
	public BlockVector3 getDimensions() {
	        return region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
	}

	@Override
	public BlockVector3 getOrigin() {
		return origin;
	}

	@Override
	public Region getRegion() {
		return region;
	}

	@Override
	public void setOrigin(BlockVector3 origin) {
		this.origin = origin;
	}


	@Override
	public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
		return setBlock(position, block);
	}

}
