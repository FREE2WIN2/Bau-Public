package de.AS.Bau.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Listener.onPlayerMove;
import de.AS.Bau.Tools.DesignTool;
import de.AS.Bau.Tools.Stoplag;
import de.AS.Bau.utils.Scheduler;

public class ScoreBoardBau {

	public static HashMap<UUID, ScoreBoardBau> playersScoreboard = new HashMap<>();

	private Scheduler scheduler;
	Player p;

	public ScoreBoardBau(Player p) {
		scheduler = new Scheduler();
		this.p = p;
		playersScoreboard.put(p.getUniqueId(), this);
		scheduler.setTask(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				cmdUpdate(p);
			}
		}, 0, 600));
	}

	public static ScoreBoardBau getS(Player p) {
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
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		List<String> rgIDs = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()));
		if (rgIDs.size() > 0) {
			// String rgID = rgIDs.get(0);
			ProtectedRegion rg = regions.getRegion(rgIDs.get(0));
			String returnString;
			if (rg.getFlag(Main.TntExplosion) == State.ALLOW) {
				// System.out.println("tnt on");
				returnString = StringGetterBau.getString(p, "boardOn");
				return returnString;
			} else {
				// System.out.println("tnt off");
				returnString = StringGetterBau.getString(p, "boardOff");
				return returnString;
			}
		} else {
			return StringGetterBau.getString(p, "boardOff");
		}

	}

	private String getSl() {
		String rgID = onPlayerMove.playersLastPlot.get(p);
		if (Stoplag.getStatus(p.getWorld().getName(), rgID)) {
			// System.out.println("sl on");
			return StringGetterBau.getString(p, "boardOn");
		} else {
			// System.out.println("sl off");
			return StringGetterBau.getString(p, "boardOff");
		}

	}

	public static void cmdUpdate(Player p) {
		getS(p).update();
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
		if (DesignTool.playerHasDtOn.containsKey(p.getUniqueId())) {
			if (DesignTool.playerHasDtOn.get(p.getUniqueId())) {
				// System.out.println("dt on");
				return StringGetterBau.getString(p, "boardOn");
			} else {
				// System.out.println("dt off");
				return StringGetterBau.getString(p, "boardOff");
			}
		} else {
			return "§cError ";
		}

	}

	private String getPlotNumber() {
		return onPlayerMove.playersLastPlot.get(p).replace("plot", "");

	}

	public void cancel() {
		scheduler.cancel();
	}

}
