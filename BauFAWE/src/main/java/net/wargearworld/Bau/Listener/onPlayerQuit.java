package net.wargearworld.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.Tools.AutoCannonReloader;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor.TestBlockEditorCore;

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
