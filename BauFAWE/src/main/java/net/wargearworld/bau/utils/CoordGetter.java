package net.wargearworld.bau.utils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldedit.math.BlockVector3;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.world.plotss.Plot;

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
        ConfigurationSection section = Main.getPlugin().getConfig()
                .getConfigurationSection("sizes." + tier);
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        return BlockVector3.at(x, y, z);
    }

    public static BlockVector3 locToVec(Location loc) {
        return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

}
