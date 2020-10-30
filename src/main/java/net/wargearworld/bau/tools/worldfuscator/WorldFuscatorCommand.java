package net.wargearworld.bau.tools.worldfuscator;

import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.utils.PacketMapChunk;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WorldFuscatorCommand implements TabExecutor {
    private CommandHandel commandHandel;

    public WorldFuscatorCommand(JavaPlugin main) {
        main.getCommand("worldfuscator").setTabCompleter(this);
        main.getCommand("worldfuscator").setExecutor(this);
        commandHandel = new CommandHandel("worldfuscator", Main.prefix, MessageHandler.getInstance());
        commandHandel.setCallback(s -> {
            BauPlayer player = BauPlayer.getBauPlayer(getPlayer(s));
            Plot currentPlot = player.getCurrentPlot();
            currentPlot.setWorldFuscated(!currentPlot.isWorldFuscated());
            if (currentPlot.isWorldFuscated()) {
                MessageHandler.getInstance().send(getPlayer(s), "worldfusactor_activated", currentPlot.getId().replace("plot", ""));
            } else {
                MessageHandler.getInstance().send(getPlayer(s), "worldfusactor_deactivated", currentPlot.getId().replace("plot", ""));
            }
            for (Player p : getPlayer(s).getWorld().getPlayers()) {
                ScoreBoardBau.getS(p).update();
                sendChunks(p);
            }
        });
    }

    private Player getPlayer(ArgumentList s) {
        return ((BukkitCommandPlayer) s.getPlayer()).getPlayer();
    }

    public void sendChunks(Player p) {
        World world = p.getWorld();
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

        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer((Player) commandSender);
        return commandHandel.execute(commandPlayer, MessageHandler.getInstance().getLanguage(((Player) commandSender).getUniqueId()), strings);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        List<String> ret = new ArrayList<>();
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer((Player) commandSender);
        commandHandel.tabComplete(commandPlayer, MessageHandler.getInstance().getLanguage(((Player) commandSender).getUniqueId()), strings, ret);
        return ret;

    }
}
