package net.wargearworld.bau.commands;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.plot.PlotPattern;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.player.BukkitCommandPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static net.wargearworld.command_manager.arguments.DynamicListArgument.dynamicList;
import static net.wargearworld.command_manager.nodes.ArgumentNode.argument;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PlotCommand implements TabExecutor {

    private CommandHandel commandHandle;

    public PlotCommand(JavaPlugin plugin) {
        plugin.getCommand("plot").setExecutor(this);
        plugin.getCommand("plot").setTabCompleter(this);

        commandHandle = new CommandHandel("plot", Main.prefix, MessageHandler.getInstance());
        commandHandle
                .addSubNode(argument("plotname", dynamicList("plotname", s -> {
                    Player p = getPlayer(s.getArgumentList());
                    Set<String> out = new TreeSet<>();
                    for (PlotPattern plotPattern : WorldManager.get(p.getWorld()).getTemplate().getPlots()) {
                        out.add(plotPattern.getID());
                    }
                    return out;
                }))
                        .setCallback(s -> {
                            Player p = getPlayer(s);
                            p.teleport(WorldManager.get(p.getWorld()).getPlot(s.getString("plotname")).getTeleportPoint());
                        }));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command arg1,
                                      String arg2, String[] args) {
        Player p = (Player) sender;
        List<String> ret = new ArrayList<>();
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandle.tabComplete(commandPlayer, args, ret);
        return ret;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player p = (Player) sender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        return commandHandle.execute(commandPlayer, args);
    }
}
