package net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.iterators;

import java.util.Iterator;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class SideIterator implements Iterator<Region> {
	Region origin;
	BlockVector3 startPoint;
	BlockVector3 maxPoint;
	int zstart;
	public SideIterator(Region rg) {
		this.origin = rg;
		startPoint = rg.getMinimumPoint();
		maxPoint = rg.getMaximumPoint();
		zstart = startPoint.getBlockZ();
	}

	@Override
	public boolean hasNext() {
		if (startPoint == null) {
			return false;
		}
		return true;
	}

	@Override
	public Region next() {
		Region out = new CuboidRegion(startPoint,
				BlockVector3.at(maxPoint.getX(), startPoint.getY(), startPoint.getZ()));
		calcNewStartPoint();
		return out;
	}

	private void calcNewStartPoint() {
		int z = startPoint.getZ() + 2;
		int y = startPoint.getY();
		if(z>maxPoint.getZ()) {
			z=origin.getMinimumPoint().getZ();
			if(zstart == origin.getMinimumPoint().getZ()) {
				z++;
				zstart++;
			}else {
				zstart--;
			}
			y+=1;
			if(y>maxPoint.getY()) {
				startPoint = null;
				return;
			}
		}
		startPoint = BlockVector3.at(origin.getMinimumPoint().getX(), y, z);
	}

}
