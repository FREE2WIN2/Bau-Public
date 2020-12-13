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
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import static net.wargearworld.bau.utils.CommandUtil.getPlayer;
import static net.wargearworld.command_manager.arguments.IntegerArgument.integer;
import static net.wargearworld.command_manager.nodes.ArgumentNode.argument;
import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
public class ExplosionCacheListener implements Listener {

    public ExplosionCacheListener() {

        Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
        CommandHandel commandHandel = TNT.commandHandle;
        commandHandel
                .addSubNode(literal("protectCannons").setCallback(s -> allowExplosion(s, 0)))
                .addSubNode(literal("unprotectCannons")
                        .setCallback(s -> allowExplosion(s, 30))
                        .addSubNode(argument("Duration", integer(1))
                                .setCallback(s -> allowExplosion(s, s.getInt("Duration")))));


    }

    private void allowExplosion(ArgumentList s, int seconds) {
        Player p = getPlayer(s);
        Location loc = p.getLocation();
        BauWorld bauWorld = WorldManager.get(loc.getWorld());
        if (bauWorld == null)
            return;
        Plot plot = bauWorld.getPlot(loc);
        if (plot == null)
            return;
        plot.deactivateExplosionCache(seconds, bauWorld);
        if (seconds > 0) {
            for (Player player : plot.getPlayers(bauWorld)) {
                MessageHandler.getInstance().send(player, "tnt_allowExplosion_activated", seconds + "");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityExplodeHandler(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        Location loc = event.getLocation();
        ExplosionCache explosionCache = getExplosionCache(loc);
        if (explosionCache != null)
            explosionCache.handleExplode(event);
        explosionCache.handleExplode(event);
    }

    @EventHandler
    public void entityPrimeEvent(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        Location loc = event.getLocation();
        ExplosionCache explosionCache = getExplosionCache(loc);
        if (explosionCache != null)
            explosionCache.onEntityPrime(event);
    }

    private ExplosionCache getExplosionCache(Location loc) {
        BauWorld world = WorldManager.get(loc.getWorld());
        if (world == null)
            return null;
        Plot plot = world.getPlot(loc);
        if (plot == null)
            return null;
        return plot.getExplosionCache();
    }
}
