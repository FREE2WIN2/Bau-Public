package net.wargearworld.Bau.Listener;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Plots.Plots;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.Tools.DesignTool;
import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.ItemStackCreator;
import net.wargearworld.StringGetter.Language;

public class onPlayerJoin implements Listener {

	public onPlayerJoin(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		// sprache
		Player p = e.getPlayer();
		DesignTool.playerHasDtOn.put(p.getUniqueId(), false);
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		// p.teleport(new Location(Bukkit.getWorld("world"), 0, 30, 0));
		String lang = DBConnection.getLanguage(p);
		MessageHandler.playersLanguage.put(p.getUniqueId(), Language.valueOf(lang.toUpperCase()));
		Main main = Main.getPlugin();
		// has own gs?
		String path = main.getCustomConfig().getString("Config.path");
		File gs = new File(path + "/" + p.getUniqueId().toString());
		if (!DBConnection.hasOwnPlots(p.getName()) && !gs.exists()) {
			// Bukkit.createWorld((WorldCreator) WorldCreator.name("test").createWorld());
			// wenn nicht-> erstellen und hinteleportieren
			WorldManager.createWorldDir(p);
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "plotGenerating"));
		}
		World world = WorldManager.loadWorld(p.getUniqueId().toString());

		String spawnPlot =  Plots.getJoinPlot(p.getUniqueId());
		onPlayerMove.playersLastPlot.put(p.getUniqueId(), spawnPlot);
		Location loc = CoordGetter.getTeleportLocation(world,spawnPlot);
		p.teleport(loc);
		// wenn ja-> teleportieren
		// item
		p.getInventory().setItem(0, ItemStackCreator.createNewItemStack(Material.NETHER_STAR, "§6GUI"));
		new ScoreBoardBau(p);

	}

}
