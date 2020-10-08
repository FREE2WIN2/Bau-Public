package net.wargearworld.bau.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.tools.AutoCannonReloader;
import net.wargearworld.bau.tools.testBlockSlave.TestBlockSlaveCore;
import net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.TestBlockEditorCore;

public class onPlayerQuit implements Listener {
public onPlayerQuit(JavaPlugin plugin) {
	Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
}
@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerLeaveevent(PlayerQuitEvent e) {
	Player p = e.getPlayer();
	onPlayerMove.playersLastPlot.remove(p.getUniqueId());
	ScoreBoardBau.getS(p).cancel();
	AutoCannonReloader.deleteRecord(p);
	TestBlockSlaveCore.playersTestBlockSlave.remove(p.getUniqueId());
	TestBlockEditorCore.playersTestBlockEditor.remove(p.getUniqueId());
	
	BauPlayer.remove(p.getUniqueId());
}
}
