package net.wargearworld.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.Tools.AutoCannonReloader;
import net.wargearworld.Bau.Tools.DesignTool;
import net.wargearworld.Bau.Tools.FernzuenderListener;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor.TestBlockEditorCore;

public class onPlayerQuit implements Listener {
public onPlayerQuit(JavaPlugin plugin) {
	Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
}
@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerLeaveevent(PlayerQuitEvent e) {
	Player p = e.getPlayer();
	onPlayerMove.playersLastPlot.remove(p);
	DesignTool.playerHasDtOn.remove(p.getUniqueId());
	ScoreBoardBau.getS(p).cancel();
	FernzuenderListener.playersDetonator.remove(p.getUniqueId());
	AutoCannonReloader.deleteRecord(p);
	TestBlockSlaveCore.playersTestBlockSlave.remove(p.getUniqueId());
	TestBlockEditorCore.playersTestBlockEditor.remove(p.getUniqueId());
}
}
