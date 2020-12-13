package net.wargearworld.bau.listener;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class PlayerMovement implements Listener {
    public static HashMap<UUID, String> playersLastPlot = new HashMap<>();

    public PlayerMovement(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {

        Player p = e.getPlayer();
        if (!p.getWorld().getName().equals("world")) {

            String rgID = WorldGuardHandler.getPlotId(e.getTo());
            /*You have to be in a Region to move(make the Region out of the regions so big.)*/

            if (rgID == null && !p.hasPermission("bau.move.bypass")) {
                p.teleport(e.getFrom());
                e.setCancelled(true);
                return;
            }
            if (rgID == null) {
                return;
            }
            if (!rgID.equals(playersLastPlot.get(p.getUniqueId())) && !rgID.equals("allplots")) {
                playersLastPlot.put(p.getUniqueId(), rgID);
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    ScoreBoardBau.cmdUpdate(p);
                }, 1);
            }

        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (e.getTo().getWorld().getName().contains("test") && !p.hasPermission("supporter")) {
            e.setCancelled(true);
            p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "noPlotMember"));
            return;
        }
        if (e.getTo().getWorld() == e.getFrom().getWorld()) {
            ScoreBoardBau.cmdUpdate(p);
            return;
        }

        if (p.hasPermission("moderator")) {
            return;
        }
        BauWorld world = WorldManager.get(e.getTo().getWorld());
        if (!world.isAuthorized(p.getUniqueId())) {
            // wenn er nicht owner und nicht Member ist
            e.setCancelled(true);
            if(world instanceof TeamWorld){
                if(((TeamWorld)world).getTeam().isNewcomer(p.getUniqueId())){
                    p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "team_world_newcomer"));
                    return;
                }
            }
            p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "noPlotMember"));

            return;
        }
        BauWorld from = WorldManager.get(e.getFrom().getWorld());
        if (from != null) {
            from.leave(p);
        }
    }
}
