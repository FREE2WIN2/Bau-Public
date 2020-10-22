package net.wargearworld.bau.tools;

import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.InvisibleNode.invisible;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;

import java.awt.datatransfer.Clipboard;
import java.util.*;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import net.wargearworld.bau.utils.*;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.worldedit.WorldEditHandler;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.Arguments.StringArgument;
import org.bukkit.scheduler.BukkitTask;

public class PlotResetter implements TabExecutor {

    private CommandHandel commandHandle;

    public PlotResetter() {
//		new CommandManager(MessageHandler.getInstance());
        commandHandle = new CommandHandel("plotreset", Main.prefix, Main.getPlugin());
        commandHandle.setCallback(s -> {
            resetRegion(s.getPlayer(), false);
        });
        commandHandle.addSubNode(literal("undo").setCallback(s -> {
            undo(s);
        }));

        commandHandle.addSubNode(
                invisible(
                        argument("UUID", new StringArgument())
                                .setCallback(s -> {
                                    if (s.getString("UUID").equalsIgnoreCase("undo")) {
                                        undo(s);
                                    } else {
                                        resetRegion(s.getPlayer(), true);
                                    }
                                })));


    }

    private void undo(ArgumentList s) {
        Player p = s.getPlayer();
        BauPlayer player = BauPlayer.getBauPlayer(p);
        Plot plot = player.getCurrentPlot();
        if (player.getCurrentPlot().undo(p.getWorld())) {
            MessageHandler.getInstance().send(s.getPlayer(), "plotreset_undo", plot.getId().replace("plot", ""));
        } else {
            MessageHandler.getInstance().send(s.getPlayer(), "plotreset_noUndo", plot.getId().replace("plot", ""));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            return commandHandle.execute(p, MessageHandler.getInstance().getLanguage(p), args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command arg1,
                                      String arg2, String[] args) {
        List<String> out = new ArrayList<>();
        Player p = (Player) sender;
        commandHandle.tabComplete(p, MessageHandler.getInstance().getLanguage(p), args, out);
        return out;
    }

    public static void resetRegion(Player p, boolean confirmed) {
//		int rgIDint = Integer.parseInt(rgID.replace("plot", ""));
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        Plot current = bauPlayer.getCurrentPlot();
        String rgID = current.getId();
        if (!confirmed) {
            JsonCreater creater1 = new JsonCreater(Main.prefix
                    + MessageHandler.getInstance().getString(p, "delePlotConfirmation", rgID.replace("plot", "")));
            JsonCreater creater2 = new JsonCreater(
                    MessageHandler.getInstance().getString(p, "deletePlotHere", rgID.replace("plot", "")));
            creater2.addHoverEvent(
                    MessageHandler.getInstance().getString(p, "delePlotHover", rgID.replace("plot", "")));
            creater2.addClickEvent("/plotreset " + p.getUniqueId(), ClickAction.RUN_COMMAND);
            creater1.addJson(creater2).send(p);
        } else {
            String plotName = rgID.replace("plot", "");
            Main.send(p, "delePlot", plotName);
            Main.send(p,"delePlot_undo",plotName);
            // für jede Zeile rgid festlegen
            ProtectedRegion rg = Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(p.getWorld()))).getRegion(rgID);

            current.reset(p.getWorld());
            /* reset*/
            calcBlocks(rg.getMinimumPoint(), rg.getMaximumPoint(), p.getWorld(),current);
        }
    }

    private static void calcBlocks(BlockVector3 minimumPoint, BlockVector3 maximumPoint, World world, Plot current) {
        boolean sl = current.getSL();
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), () -> {
        List<Block> list = new LinkedList<>();
        int maxBlockChangePerTick = WorldEditHandler.maxBlockChangePerTick;
        int blockCount = 0;
            for (int x = minimumPoint.getBlockX(); x <= maximumPoint.getBlockX(); x++) {
                for (int y = minimumPoint.getBlockY(); y <= maximumPoint.getBlockY(); y++) {
                    for (int z = minimumPoint.getBlockZ(); z <= maximumPoint.getBlockZ(); z++) {
                        Block b = world.getBlockAt(x, y, z);
                        if (b.getType() != Material.AIR) {
                            list.add(b);
                            blockCount++;
                        }
                        if(blockCount == maxBlockChangePerTick){
                            remove(new LinkedList<>(list),current,sl);
                            list = new LinkedList<>();
                        }
                    }
                }
            }
            remove(list, current,sl);
        },0,1);
    }

    private static void remove(List<Block> list, Plot current,boolean stoplag) {

        Scheduler scheduler = new Scheduler();
        if(!stoplag){
        current.setSL(true);
        }
        scheduler.setTask(
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    for(Block b:list) {
                        b.setType(Material.AIR, false);
                    }
                    if(!stoplag){
                        current.setSL(false);
                    }
                    scheduler.cancel();
                }, 0));
    }

}
