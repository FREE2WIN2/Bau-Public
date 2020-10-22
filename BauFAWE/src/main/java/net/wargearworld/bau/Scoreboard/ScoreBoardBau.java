package net.wargearworld.bau.scoreboard;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.listener.onPlayerMove;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.Stoplag;
import net.wargearworld.bau.utils.Scheduler;
import net.wargearworld.bau.world.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ScoreBoardBau {

    public static HashMap<UUID, ScoreBoardBau> playersScoreboard = new HashMap<>();

    private Scheduler scheduler;
    private Player p;
    private BauPlayer player;

    public ScoreBoardBau(Player p) {
        scheduler = new Scheduler();
        this.p = p;
        player = BauPlayer.getBauPlayer(p);
        playersScoreboard.put(p.getUniqueId(), this);
        scheduler.setTask(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

            @Override
            public void run() {
                update();
            }
        }, 0, 600));
    }

    public static ScoreBoardBau getS(Player p) {
        if (!playersScoreboard.containsKey(p.getUniqueId())) {
            return new ScoreBoardBau(p);
        }
        return playersScoreboard.get(p.getUniqueId());
    }

    private String getTime() {
        String Time;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        Time = formatter.format(date);
        return Time;
    }

    private String getTNT(Plot currentPlot) {
        if (currentPlot.getTNT()) {
            // System.out.println("tnt on");
            return MessageHandler.getInstance().getString(p, "boardOn");
        } else {
            // System.out.println("tnt off");
            return MessageHandler.getInstance().getString(p, "boardOff");
        }
    }

    private String getSl() {
        String rgID = onPlayerMove.playersLastPlot.get(p.getUniqueId());
        if (Stoplag.getStatus(p.getWorld(), rgID)) {
            // System.out.println("sl on");
            return MessageHandler.getInstance().getString(p, "boardOn");
        } else {
            // System.out.println("sl off");
            return MessageHandler.getInstance().getString(p, "boardOff");
        }

    }

    public static void cmdUpdate(Player p) {
        if (p == null) {
            return;
        }
        if (onPlayerMove.playersLastPlot.containsKey(p.getUniqueId())) {
            getS(p).update();
        }
    }

    public void update() {
        Scoreboard board = p.getScoreboard() == null ? board = Bukkit.getScoreboardManager().getNewScoreboard()
                : p.getScoreboard();
        Objective obj;
        if (board.getObjective(p.getName()) != null) {
            obj = board.getObjective(p.getName());
            obj.unregister();
        }
        Plot currentPlot = player.getCurrentPlot();
        if(currentPlot == null)
            return;
        obj = board.registerNewObjective(p.getName(), p.getName() + "bbbb", p.getName() + "cccc");
        obj.setDisplayName("§6Time: §c" + getTime());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore("§6Plot: §c" + currentPlot.getId().replace("plot", "")).setScore(11);
        obj.getScore("§1§8§l§m               §r").setScore(10);
        obj.getScore("§6TNT: " + getTNT(currentPlot)).setScore(9);
        obj.getScore("§2§8§l§m               §r").setScore(8);
        obj.getScore("§6Stoplag: " + getSl()).setScore(7);
        obj.getScore("§3§8§l§m               §r").setScore(6);
        obj.getScore("§6DesignTool: " + getDt()).setScore(5);
        obj.getScore("§4§8§l§m               §r").setScore(4);
        obj.getScore("§6WaterRemover: " + getWaterRemover(currentPlot)).setScore(3);
        obj.getScore("§5§8§l§m               §r").setScore(2);
        obj.getScore("§6WorldFuscator: " + getWorldFuscator(currentPlot)).setScore(1);
        p.setScoreboard(board);
    }

    private String getWorldFuscator(Plot currentPlot) {
        if (currentPlot.isWorldFuscated()) {
            // System.out.println("dt on");
            return MessageHandler.getInstance().getString(p, "boardOn");
        } else {
            // System.out.println("dt off");
            return MessageHandler.getInstance().getString(p, "boardOff");
        }
    }

    private String getWaterRemover(Plot currentPlot) {
        if (currentPlot.getWaterRemover() != null) {
            // System.out.println("dt on");
            return MessageHandler.getInstance().getString(p, "boardOn");
        } else {
            // System.out.println("dt off");
            return MessageHandler.getInstance().getString(p, "boardOff");
        }
    }

    private String getDt() {
        if (player.getDT()) {
            // System.out.println("dt on");
            return MessageHandler.getInstance().getString(p, "boardOn");
        } else {
            // System.out.println("dt off");
            return MessageHandler.getInstance().getString(p, "boardOff");
        }
    }

    @SuppressWarnings("deprecation")
    public void cancel() {
        scheduler.cancel();
        playersScoreboard.remove(p.getUniqueId());
        try {
            this.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
