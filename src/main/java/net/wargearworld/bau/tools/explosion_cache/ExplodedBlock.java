package net.wargearworld.bau.tools.explosion_cache;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class ExplodedBlock {
    private Location loc;
    /*private Material type;
    private BlockData blockData*/;

    public ExplodedBlock(Block block) {
        this.loc = block.getLocation();
        /*this.type = block.getType();
        this.blockData = block.getBlockData();
        System.out.println(type.name() + " at " + loc);*/
    }

    public void place(){
        Block b = loc.getBlock();
        b.setType(Material.AIR);
        /*loc.getBlock().setType(type);
        b.setBlockData(blockData,false);*/
    }
}
