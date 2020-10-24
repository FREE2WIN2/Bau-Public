package net.wargearworld.bau.tools.worldfuscator;

import com.pro_crafting.mc.worldfuscator.engine.guard.WorldFuscatorGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.plot.Plot;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WGAFightGuard implements WorldFuscatorGuard {
    WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
    private final Map<String, RegionManager> regionManagers = new ConcurrentHashMap<>();

    @Override
    public boolean hasAreaRights(Player player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, World world) {
        BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);
        RegionManager regionManager = getRegionManager(world);
        String minRgID = regionManager.getApplicableRegionsIDs(min).get(0);
        String maxRgID = regionManager.getApplicableRegionsIDs(max).get(0);

        BauWorld bauWorld = WorldManager.get(world);
        Plot minPlot = bauWorld.getPlot(minRgID);
        Plot maxPlot = bauWorld.getPlot(maxRgID);
        if (minPlot == null && maxPlot == null) {
            return true;
        }
        if(minPlot == null){
            return maxPlot.calcWorldFuscator(max);
        }
        if(maxPlot == null){
            return minPlot.calcWorldFuscator(min);
        }
        return minPlot.calcWorldFuscator(min) || maxPlot.calcWorldFuscator(max);
    }

    @Override
    public boolean hasRights(Player player, int x, int y, int z, World world) {
        BlockVector3 vector3 = BlockVector3.at(x, y, z);
        RegionManager regionManager = getRegionManager(world);
        String rgID = regionManager.getApplicableRegionsIDs(vector3).get(0);

        BauWorld bauWorld = WorldManager.get(world);
        Plot plot = bauWorld.getPlot(rgID);
        if(plot == null){
            return true;
        }
        return plot.calcWorldFuscator(vector3);
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }

    protected RegionManager getRegionManager(World world) {
        RegionManager manager = regionManagers.get(world.getName());
        if (manager == null) {
            manager = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            regionManagers.put(world.getName(), manager);
        }

        return manager;
    }
}
