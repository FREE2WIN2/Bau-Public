package net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.iterators;

import java.util.Iterator;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class RoofIterator implements Iterator<Region> {


    Region origin;
    BlockVector3 startPoint;
    BlockVector3 maxPoint;
    int xstart;

    public RoofIterator(Region rg) {
        this.origin = rg;
        startPoint = rg.getMinimumPoint();
        maxPoint = rg.getMaximumPoint();
        xstart = startPoint.getX();
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
                BlockVector3.at(startPoint.getX(), maxPoint.getBlockY(), startPoint.getZ()));
        calcNewStartPoint();
        return out;
    }

    private void calcNewStartPoint() {
        int x = startPoint.getX() + 2;
        int z = startPoint.getZ();
        if (x > maxPoint.getX()) {
            x = origin.getMinimumPoint().getX();
            if (xstart == origin.getMinimumPoint().getX()) {
                xstart++;
                x++;
            } else {
                xstart--;
            }
            z += 1;
            if (z > maxPoint.getZ()) {
                startPoint = null;
                return;
            }
        }
        startPoint = BlockVector3.at(x, origin.getMinimumPoint().getY(), z);
    }

}
