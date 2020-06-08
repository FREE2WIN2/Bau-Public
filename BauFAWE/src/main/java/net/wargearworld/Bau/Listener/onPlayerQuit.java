package net.wargearworld.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.Tools.AutoCannonReloader;
import net.wargearworld.Bau.Tools.DesignTool;
import net.wargearworld.Bau.Tools.FernzuenderListener;

public class onPlayerQuit implements Listener {
public onPlayerQuit(JavaPlugin plugin) {
	Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
}
@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerLeaveevent(PlayerQuitEvent e) {
	onPlayerMove.playersLastPlot.remove(e.getPlayer());
	DesignTool.playerHasDtOn.remove(e.getPlayer().getUniqueId());
	ScoreBoardBau.getS(e.getPlayer()).cancel();
	FernzuenderListener.playersDetonator.remove(e.getPlayer().getUniqueId());
	AutoCannonReloader.deleteRecord(e.getPlayer());
}
}
