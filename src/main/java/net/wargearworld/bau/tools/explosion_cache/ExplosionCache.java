package net.wargearworld.bau.tools.explosion_cache;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.TNT;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExplosionCache {
    private Map<UUID, Boolean> tnts;

    public ExplosionCache() {
        tnts = new HashMap<>();
       }

    public void handleExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        BauWorld bauWorld = WorldManager.get(loc.getWorld());
        if (bauWorld == null)
            return;
        Plot plot = bauWorld.getPlot(loc);
        if (plot == null)
            return;
        if(plot.isDeactivatedExplosionCache())
            return;
        int z = plot.getTeleportPoint().getBlockZ();
        Boolean primeZSmallerThanMiddleZ = tnts.get(event.getEntity().getUniqueId());
        Boolean explosionZSmallerThanMiddleZ = event.getEntity().getLocation().getBlockZ() < z;
        if (primeZSmallerThanMiddleZ == null)
            return;

        boolean sameTeam = !explosionZSmallerThanMiddleZ ^ primeZSmallerThanMiddleZ;
        if (sameTeam) {
            tnts.remove(event.getEntity().getUniqueId());
            event.blockList().clear();
            return;
        }
        if (getTnts(event.getLocation().getWorld()) == 1) {
            for (Player player : plot.getPlayers(bauWorld)) {
                MessageHandler.getInstance().send(player, "explosion_cached");
            }
        }
    }

    public void onEntityPrime(EntitySpawnEvent event) {
        Location loc = event.getLocation();
        BauWorld bauWorld = WorldManager.get(loc.getWorld());
        if (bauWorld == null)
            return;
        Plot plot = bauWorld.getPlot(loc);
        if (plot == null)
            return;
        int z = plot.getTeleportPoint().getBlockZ();
        tnts.put(event.getEntity().getUniqueId(), loc.getZ() < z);
    }

    private int getTnts(World w) {
        int count = 0;
        for (Entity entity : w.getEntities()) {
            if (entity.getType() == EntityType.PRIMED_TNT) {
                count++;
            }
        }
        return count;
    }
}
