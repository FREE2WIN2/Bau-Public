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
import de.AS.Bau.cmds.dt;

public class ScoreBoardBau {

	public static HashMap<UUID, Integer> Scheduler_ids_Scoreboard = new HashMap<UUID, Integer>();
	public static HashMap<Player, Boolean> stopPlayersScoreboard = new HashMap<>();

	private static HashMap<Player, Integer> taskID = new HashMap<>();

	public ScoreBoardBau(Player p) {
		stopPlayersScoreboard.put(p, false);
		cmdUpdate(p);
		ScoreBoardBau.taskID.put(p,
				Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						if (stopPlayersScoreboard.get(p)) {
							cancel(p);
							stopPlayersScoreboard.remove(p);
						} else {
							cmdUpdate(p);
						}
					}
				}, 600, 600));
	}

	public ScoreBoardBau getS() {
		return this;
	}

	public static String getTime() {
		String Time;
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		Date date = new Date();
		Time = formatter.format(date);
		return Time;
	}

	public static String getTNT(Player p) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		List<String> rgIDs = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()));
		if (rgIDs.size() > 0) {
			//String rgID = rgIDs.get(0);
			ProtectedRegion rg = regions.getRegion(rgIDs.get(0));
			String returnString;
			if (rg.getFlag(Main.TntExplosion) == State.ALLOW) {
				//System.out.println("tnt on");
				returnString = StringGetterBau.getString(p, "boardOn");
				return returnString;
			} else {
				//System.out.println("tnt off");
				returnString = StringGetterBau.getString(p, "boardOff");
				return returnString;
			}
		} else {
			return StringGetterBau.getString(p, "boardOff");
		}

	}

	public static String getSl(Player p) {
		Main main = Main.getPlugin();
		String rgID = onPlayerMove.playersLastPlot.get(p);
			if (main.getCustomConfig().getString("stoplag."+p.getWorld().getName()+"."+rgID).equals("an")) {
				//System.out.println("sl on");
				return StringGetterBau.getString(p, "boardOn");
			} else {
				//System.out.println("sl off");
				return StringGetterBau.getString(p, "boardOff");
			}

	}

	public static void cmdUpdate(Player p) {
		Scoreboard board = p.getScoreboard() == null ? board=Bukkit.getScoreboardManager().getNewScoreboard() : p.getScoreboard();
		Objective obj;
		if(board.getObjective(p.getName()) != null) {
			obj = board.getObjective(p.getName());
			obj.unregister();
		}
		obj= board.registerNewObjective(p.getName(), p.getName() +"bbbb", p.getName() + "cccc");
		obj.setDisplayName("§6Time: §c" + getTime());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.getScore("§6Plot: §c" + getPlotNumber(p)).setScore(7);
		obj.getScore("§8§l§m             §r  ").setScore(6);
		obj.getScore("§6TNT: " + getTNT(p)).setScore(5);
		obj.getScore("§8§l§m             §r ").setScore(4);
		obj.getScore("§6Stoplag: " + getSl(p)).setScore(3);
		obj.getScore("§8§l§m             §r").setScore(2);
		obj.getScore("§6DesignTool: " + getDt(p)).setScore(1);
		p.setScoreboard(board);
	}

	private static String getDt(Player p) {
		if (dt.playerHasDtOn.containsKey(p)) {
			if (dt.playerHasDtOn.get(p)) {
				//System.out.println("dt on");
				return StringGetterBau.getString(p, "boardOn");
			} else {
				//System.out.println("dt off");
				return StringGetterBau.getString(p, "boardOff");
			}
		} else {
			return "§cError ";
		}

	}

	private static String getPlotNumber(Player p) {
//		RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
//				.get(BukkitAdapter.adapt(p.getWorld()));
//		if (rm.getApplicableRegions(
//				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())).getRegions()
//				.size() > 0) {
//			System.out.println(rm
//					.getApplicableRegionsIDs(
//							BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
//					.get(0).replace("plot", ""));
//			return rm
//					.getApplicableRegionsIDs(
//							BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
//					.get(0).replace("plot", "");
		
//		} else {
//			return "§cError";
//		}
	///	System.out.println(onPlayerMove.playersLastPlot.get(p).replace("plot", ""));
		return onPlayerMove.playersLastPlot.get(p).replace("plot", "");
		
	}

	private void cancel(Player p) {
		Bukkit.getScheduler().cancelTask(taskID.get(p));
		taskID.remove(p);
	}

}
