package net.wargearworld.bau.scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.listener.onPlayerMove;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.Stoplag;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import net.wargearworld.bau.utils.Scheduler;

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

	private String getTNT() {

		// String rgID = rgIDs.get(0);
		if (p == null) {
			scheduler.cancel();
			return null;
		}
		if (!onPlayerMove.playersLastPlot.containsKey(p.getUniqueId())) {
			scheduler.cancel();
			return null;
		}

		String rgID = onPlayerMove.playersLastPlot.get(p.getUniqueId());
		if (rgID == null) {
			scheduler.cancel();
			return null;
		}
		ProtectedRegion rg = WorldGuardHandler.getRegion(rgID, BukkitAdapter.adapt(p.getWorld()));
		if (rg != null) {
			if (rg.getFlag(Main.TntExplosion) == State.ALLOW) {
				// System.out.println("tnt on");
				return MessageHandler.getInstance().getString(p, "boardOn");
			} else {
				// System.out.println("tnt off");
				return MessageHandler.getInstance().getString(p, "boardOff");
			}
		} else {
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
		obj = board.registerNewObjective(p.getName(), p.getName() + "bbbb", p.getName() + "cccc");
		obj.setDisplayName("§6Time: §c" + getTime());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.getScore("§6Plot: §c" + getPlotNumber()).setScore(7);
		obj.getScore("§8§l§m             §r  ").setScore(6);
		obj.getScore("§6TNT: " + getTNT()).setScore(5);
		obj.getScore("§8§l§m             §r ").setScore(4);
		obj.getScore("§6Stoplag: " + getSl()).setScore(3);
		obj.getScore("§8§l§m             §r").setScore(2);
		obj.getScore("§6DesignTool: " + getDt()).setScore(1);
		p.setScoreboard(board);
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

	private String getPlotNumber() {
		if (onPlayerMove.playersLastPlot.containsKey(p.getUniqueId())) {
			return onPlayerMove.playersLastPlot.get(p.getUniqueId()).replace("plot", "");
		}
		getS(p).cancel();
		return null;
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
