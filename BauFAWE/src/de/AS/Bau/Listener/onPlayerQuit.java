package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;


import de.AS.Bau.Scoreboard.ScoreBoardBau;
import de.AS.Bau.cmds.dt;

public class onPlayerQuit implements Listener {
public onPlayerQuit(JavaPlugin plugin) {
	Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
}
@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerLeaveevent(PlayerQuitEvent e) {
	onPlayerMove.playersLastPlot.remove(e.getPlayer());
	dt.playerHasDtOn.remove(e.getPlayer().getUniqueId());
	ScoreBoardBau.stopPlayersScoreboard.put(e.getPlayer(), true);
	ClickListener.playersDetonator.remove(e.getPlayer().getUniqueId());
}
}
