package net.wargearworld.bau.tools.waterremover;

import com.sk89q.worldguard.protection.flags.StateFlag;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.plot.Plot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.ArrayList;
import java.util.List;

public class WaterRemoverListener implements Listener, TabExecutor {
    public static StateFlag waterRemoverFlag;


    private CommandHandel commandHandel;

    public WaterRemoverListener(Main main) {
        Bukkit.getPluginManager().registerEvents(this,main);
        main.getCommand("waterremover").setTabCompleter(this);
        main.getCommand("waterremover").setExecutor(this);
        commandHandel = new CommandHandel("warterremover", Main.prefix, Main.getPlugin());
        commandHandel.setCallback(s -> {
            BauPlayer player = BauPlayer.getBauPlayer(s.getPlayer());
            Plot currentPlot = player.getCurrentPlot();
            currentPlot.setWaterRemover(currentPlot.getWaterRemover()==null);
            if(currentPlot.getWaterRemover()==null){
                MessageHandler.getInstance().send(s.getPlayer(),"waterremover_deactivated", currentPlot.getId().replace("plot",""));
            }else{
                MessageHandler.getInstance().send(s.getPlayer(),"waterremover_activated", currentPlot.getId().replace("plot",""));
            }
            for(Player p: s.getPlayer().getWorld().getPlayers()){
                ScoreBoardBau.getS(p).update();
            }
        });
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        return commandHandel.execute((Player) commandSender, MessageHandler.getInstance().getLanguage(((Player) commandSender).getUniqueId()), strings);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        List<String> ret = new ArrayList<>();
        commandHandel.tabComplete((Player) commandSender, MessageHandler.getInstance().getLanguage(((Player) commandSender).getUniqueId()), strings, ret);
        return ret;

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplodeHandler(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        Location loc = event.getLocation();
        Plot plot = WorldManager.get(loc.getWorld()).getPlot(loc);
        WaterRemover waterRemover = plot.getWaterRemover();
        if (waterRemover != null) {
            waterRemover.handleExplode(event);
        }
    }

    @EventHandler
    public void entityPrimeEvent(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        Location loc = event.getLocation();
        Plot plot = WorldManager.get(loc.getWorld()).getPlot(loc);
        WaterRemover waterRemover = plot.getWaterRemover();
        if (waterRemover != null) {
            waterRemover.onEntityPrime(event);
        }
    }

}
