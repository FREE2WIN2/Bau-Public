package net.wargearworld.bau.utils;

import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.config.Sizes;
import org.bukkit.Location;

import com.sk89q.worldedit.math.BlockVector3;

import net.wargearworld.bau.tools.testBlock.testBlock.Facing;
import net.wargearworld.bau.world.plot.Plot;

public class CoordGetter {

    public static BlockVector3 getTBSPastePosition(Plot plot, Facing facing) {
        if(facing == Facing.SOUTH){
            return locToVec(plot.getTeleportPoint()).subtract(0,0,1);
        }else{
            return locToVec(plot.getTeleportPoint());
        }
    }

    public static BlockVector3 getMiddleRegionTB(Plot plot, Facing facing) {
       if(facing == Facing.SOUTH){
           return locToVec(plot.getPasteS()).subtract(0,0,1);
       }else{
           return locToVec(plot.getPasteN()).add(0,0,1);
       }
    }

    public static BlockVector3 getMaxSizeOfBlock(int tier) {
        Sizes sizes = BauConfig.getInstance().getSize(tier);
        if(sizes == null)
            return null;
        return sizes.toBlockVector();
    }

    public static BlockVector3 locToVec(Location loc) {
        return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

}
