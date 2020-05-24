package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;


import de.AS.Bau.Scoreboard.ScoreBoardBau;
import de.AS.Bau.Tools.DesignTool;
import de.AS.Bau.Tools.FernzuenderListener;

public class onPlayerQuit implements Listener {
public onPlayerQuit(JavaPlugin plugin) {
	Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
}
@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerLeaveevent(PlayerQuitEvent e) {
	onPlayerMove.playersLastPlot.remove(e.getPlayer());
	DesignTool.playerHasDtOn.remove(e.getPlayer().getUniqueId());
	ScoreBoardBau.stopPlayersScoreboard.put(e.getPlayer(), true);
	FernzuenderListener.playersDetonator.remove(e.getPlayer().getUniqueId());
}
}
