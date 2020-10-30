package net.wargearworld.bau.tools.explosion_cache;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.player.CommandPlayer;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
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

import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;
public class ExplosionCacheListener implements Listener, TabExecutor {

    private CommandHandel commandHandel;

    public ExplosionCacheListener() {
        Main.getPlugin().getCommand("explosion").setExecutor(this);
        Main.getPlugin().getCommand("explosion").setTabCompleter(this);

        Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
        commandHandel = new CommandHandel("explosion", Main.prefix, MessageHandler.getInstance());
        commandHandel.addSubNode(literal("redo").setCallback(s -> {
            redo(s);
        }));
    }

    private void redo(ArgumentList s) {
        Player p = getPlayer(s);
        WorldManager.get(p.getWorld()).getExplosionCache().redo(p);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player p = (Player) commandSender;
        CommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandel.execute(commandPlayer, MessageHandler.getInstance().getLanguage(p), args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        List<String> out = new ArrayList<>();
        Player p = (Player) commandSender;
        CommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandel.tabComplete(commandPlayer, MessageHandler.getInstance().getLanguage(p), args, out);
        return out;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplodeHandler(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        Location loc = event.getLocation();
        BauWorld world = WorldManager.get(loc.getWorld());
        if(world == null)
            return;
        System.out.println("explosion");
        ExplosionCache explosionCache = world.getExplosionCache();
        explosionCache.handleExplode(event);
    }

    @EventHandler
    public void entityPrimeEvent(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        Location loc = event.getLocation();
        BauWorld world = WorldManager.get(loc.getWorld());
        ExplosionCache explosionCache = world.getExplosionCache();
        explosionCache.onEntityPrime(event);
    }

   /* @EventHandler
    public void onBlockDisappear(BlockDestroyEvent event){
        System.out.println(event.getBlock().getType().name() + " TO " + event.getNewState().getMaterial().name());
    }*/

}
