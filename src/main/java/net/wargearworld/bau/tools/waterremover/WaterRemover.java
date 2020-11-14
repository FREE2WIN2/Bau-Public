package net.wargearworld.bau.tools.waterremover;

import net.wargearworld.bau.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WaterRemover {
    private List<SimpleEntry<Location, Integer>> explodedBlocks;
    private List<Block> waterList;
    private BukkitTask task;

    private HashMap<UUID, Boolean> tnts;
    private int z;
    public WaterRemover(int z) {
        explodedBlocks = new ArrayList<SimpleEntry<Location, Integer>>();
        waterList = new ArrayList<Block>();
        tnts = new HashMap<>();
        this.z = z;

        explodedBlocks = new ArrayList<SimpleEntry<Location, Integer>>();
        waterList = new ArrayList<Block>();
        tnts = new HashMap<>();
        task = Bukkit.getServer().getScheduler().runTaskTimer(Main.getPlugin(), new Runnable() {
            public void run() {
                wateredCheck();
                removeWater();
            }
        }, 0, 20);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public void add(Location loc) {
        this.explodedBlocks.add(new SimpleEntry<Location, Integer>(loc, 0));
    }

    private void wateredCheck() {
        for (int i = this.explodedBlocks.size() - 1; i > -1; i--) {
            if (this.explodedBlocks.get(i).getValue() >= 15) {
                Block b = this.explodedBlocks.get(i).getKey().getBlock();
                if (b.getType() == Material.WATER || b.isLiquid() || b.getBlockData() instanceof Waterlogged) {
                    this.waterList.add(b);
                }
                this.explodedBlocks.remove(i);
            } else {
                this.explodedBlocks.get(i).setValue(this.explodedBlocks.get(i).getValue() + 1);
            }
        }
    }

    private void removeWater() {
        for (int i = this.waterList.size() - 1; i > -1; i--) {
            Block current = this.waterList.get(i);
            for (Block removeBlock : getSourceBlocksOfWater(current)) {
                if (removeBlock.getBlockData() instanceof Waterlogged) {
                    Waterlogged logged = (Waterlogged) removeBlock.getBlockData();
                    logged.setWaterlogged(false);
                    removeBlock.setBlockData(logged);
                } else {
                    removeBlock.setType(Material.AIR);
                }
            }
            if (current.getType() == Material.AIR && !(current.getBlockData() instanceof Waterlogged)) {
                this.waterList.remove(i);
            }
        }
    }

    private List<Block> getSourceBlocksOfWater(Block startBlock) {
        List<Block> water = new ArrayList<Block>();
        collectBlocks(startBlock, water, new ArrayList<Block>());
        return water;
    }

    /*
     * code by: andf54
     * https://forums.bukkit.org/threads/get-the-whole-stream-of-water-or-lava.
     * 110156/ Einige kleinere Änderungen vorgenommen
     */
    public void collectBlocks(Block anchor, List<Block> collected, List<Block> visitedBlocks) {
        if (!(anchor.getType() == Material.WATER || anchor.isLiquid() || anchor.getBlockData() instanceof Waterlogged))
            return;

        if (visitedBlocks.contains(anchor))
            return;
        visitedBlocks.add(anchor);
        if (anchor.getType() == Material.WATER || anchor.getBlockData() instanceof Waterlogged) {
            collected.add(anchor);
        }

        collectBlocks(anchor.getRelative(BlockFace.UP), collected, visitedBlocks);
        collectBlocks(anchor.getRelative(BlockFace.NORTH), collected, visitedBlocks);
        collectBlocks(anchor.getRelative(BlockFace.EAST), collected, visitedBlocks);
        collectBlocks(anchor.getRelative(BlockFace.SOUTH), collected, visitedBlocks);
        collectBlocks(anchor.getRelative(BlockFace.WEST), collected, visitedBlocks);
    }

    public void handleExplode(EntityExplodeEvent event){
        event.setYield(0);
        if(tnts == null || event.getEntity() == null || tnts.get(event.getEntity().getUniqueId()) == null)
            return;
        boolean primeZSmallerThanMiddleZ = tnts.get(event.getEntity().getUniqueId());
        boolean explosionZSmallerThanMiddleZ = event.getEntity().getLocation().getBlockZ() < z;

        boolean sameTeam = ! explosionZSmallerThanMiddleZ^primeZSmallerThanMiddleZ;
        tnts.remove(event.getEntity().getUniqueId());

        if (event.blockList().isEmpty() && !sameTeam) {
            Block block = event.getLocation().getBlock();
            if (block.isLiquid()) {
                block.setType(Material.AIR);
            } else if (block.getBlockData() instanceof Waterlogged) {
                Waterlogged waterlogged = (Waterlogged) block.getBlockData();
                waterlogged.setWaterlogged(false);
                block.setBlockData(waterlogged, true);
            } else {
                return;
            }
            event.setCancelled(true);
            event.getLocation().getWorld().createExplosion(event.getLocation(), 4F);
        }
        List<Block> blocks = new ArrayList<>(event.blockList());
        for (Block b : blocks) {
            if (!(b.getType() == Material.WATER || b.isLiquid() || b.getBlockData() instanceof Waterlogged)) {
                add(b.getLocation());
            }
        }


    }

    public void onEntityPrime(EntitySpawnEvent event){
        tnts.put(event.getEntity().getUniqueId(), event.getEntity().getLocation().getZ()<z);
    }

}
