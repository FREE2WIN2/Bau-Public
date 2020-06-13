package net.wargearworld.Bau.WorldEdit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.Tools.Stoplag;

public class WorldEditPreCommand implements Listener{

	@EventHandler
	public void PreCommandProcess(PlayerCommandPreprocessEvent event) {

		String command = event.getMessage();
		Player p = event.getPlayer();
		if (command.startsWith("//paste")) {
			Stoplag.setStatusTemp(p.getLocation(), true, 5);
			ScoreBoardBau.cmdUpdate(p);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
				
				@Override
				public void run() {
					ScoreBoardBau.cmdUpdate(p);				
				}
			}, 5*20);
		}

		if (command.equalsIgnoreCase("//rotate")) {
			event.setCancelled(true);
			WorldEditHandler.rotateClipboard(p);
		}
	}
}
