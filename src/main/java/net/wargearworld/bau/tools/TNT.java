package net.wargearworld.bau.tools;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.player.BukkitCommandPlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static net.wargearworld.bau.utils.CommandUtil.getPlayer;
public class TNT implements TabExecutor, Listener {


    public static CommandHandel commandHandle;

    public TNT(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("tnt").setExecutor(this);
        plugin.getCommand("tnt").setTabCompleter(this);

        commandHandle = new CommandHandel("tnt",Main.prefix,MessageHandler.getInstance());
        commandHandle.setRequirement(s->{
            Player p = getPlayer(s);
            BauWorld bauWorld = WorldManager.get(p.getWorld());
            return bauWorld.hasRights(p.getUniqueId());
        });
        commandHandle.setCallback(s->toggleTNT(s));
    }

    private void toggleTNT(ArgumentList s) {
        Player p = getPlayer(s);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
        String rgID = regions
                .getApplicableRegionsIDs(
                        BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
                .get(0);
        ProtectedRegion rg = regions.getRegion(rgID);
        if (rg.getFlag(Main.TntExplosion) == StateFlag.State.ALLOW) {
            rg.setFlag(Main.TntExplosion, StateFlag.State.DENY);
            for (Player a : p.getWorld().getPlayers()) {
                ScoreBoardBau.cmdUpdate(a);
                a.sendTitle(MessageHandler.getInstance().getString(p, "tntDeactivatedTitle"),
                        MessageHandler.getInstance().getString(p, "tntTitleSmall").replace("%r", rgID.replace("plot", "")), 1,
                        1, 60);
            }
            p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "tntDeniedMessage"));
        } else {
            rg.setFlag(Main.TntExplosion, StateFlag.State.ALLOW);
            for (Player a : p.getWorld().getPlayers()) {
                ScoreBoardBau.cmdUpdate(a);
                a.sendTitle(MessageHandler.getInstance().getString(p, "tntActivatedTitle"),
                        MessageHandler.getInstance().getString(p, "tntTitleSmall").replace("%r", rgID.replace("plot", "")), 1,
                        1, 60);
            }
            p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "tntAllowedMessage"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTntExplosion(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) {
            return;
        }
        Entity e = event.getEntity();
        Location loc = BukkitAdapter.adapt(e.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(loc, null, Main.TntExplosion)) {
            event.blockList().clear();
        } else {

            List<Block> blockList = new ArrayList<>(event.blockList());
            for (Block block : blockList) {
                if (!query.testState(BukkitAdapter.adapt(block.getLocation()), null, Main.TntExplosion)) {
                    event.blockList().remove(block);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        return commandHandle.execute(commandPlayer, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player p = (Player) sender;
        List<String> ret = new ArrayList<>();
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandle.tabComplete(commandPlayer, args, ret);
        return ret;
    }
}
