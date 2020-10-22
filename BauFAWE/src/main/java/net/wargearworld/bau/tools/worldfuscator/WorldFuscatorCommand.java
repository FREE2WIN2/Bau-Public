package net.wargearworld.bau.tools.worldfuscator;

import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.utils.PacketMapChunk;
import net.wargearworld.bau.world.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldFuscatorCommand implements TabExecutor {
    private CommandHandel commandHandel;

    public WorldFuscatorCommand(Main main) {
        main.getCommand("worldfusactor").setTabCompleter(this);
        main.getCommand("worldfusactor").setExecutor(this);
        commandHandel = new CommandHandel("worldfusactor", Main.prefix, Main.getPlugin());
        commandHandel.setCallback(s -> {
            BauPlayer player = BauPlayer.getBauPlayer(s.getPlayer());
            Plot currentPlot = player.getCurrentPlot();
            currentPlot.setWorldFuscated(!currentPlot.isWorldFuscated());
            if (currentPlot.isWorldFuscated()) {
                MessageHandler.getInstance().send(s.getPlayer(), "worldfusactor_deactivated", currentPlot.getId().replace("plot", ""));
            } else {
                MessageHandler.getInstance().send(s.getPlayer(), "worldfusactor_activated", currentPlot.getId().replace("plot", ""));
            }
            for (Player p : s.getPlayer().getWorld().getPlayers()) {
                ScoreBoardBau.getS(p).update();
                sendChunks(p);
            }
        });
    }

    public void sendChunks(Player p) {
        World world = Bukkit.getWorld("world");
        for (Chunk chunk : world.getLoadedChunks()) {
            new PacketMapChunk(chunk).send(p);
            world.refreshChunk(chunk.getX(), chunk.getZ());
        }
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
}
