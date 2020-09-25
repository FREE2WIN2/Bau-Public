package net.wargearworld.Bau.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;

public class tnt implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if (arg3.length == 0) {
			Player p = (Player) sender;
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
			String rgID = regions
					.getApplicableRegionsIDs(
							BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
					.get(0);
			ProtectedRegion rg = regions.getRegion(rgID);
			if (rg.getFlag(Main.TntExplosion) == State.ALLOW) {
				rg.setFlag(Main.TntExplosion, State.DENY);
				for (Player a : p.getWorld().getPlayers()) {
					ScoreBoardBau.cmdUpdate(a);
					a.sendTitle(MessageHandler.getInstance().getString(p,"tntDeactivatedTitle"),
							MessageHandler.getInstance().getString(p,"tntTitleSmall").replace("%r", rgID.replace("plot", "")), 1,
							1, 60);
				}
			} else {
				rg.setFlag(Main.TntExplosion, State.ALLOW);
				for (Player a : p.getWorld().getPlayers()) {
					ScoreBoardBau.cmdUpdate(a);
					a.sendTitle(MessageHandler.getInstance().getString(p,"tntActivatedTitle"),
							MessageHandler.getInstance().getString(p,"tntTitleSmall").replace("%r", rgID.replace("plot", "")), 1,
							1, 60);
				}
			}
			
			return true;
		} else {
			return false;
		}
	}

}
