package net.wargearworld.Bau.Listener;

import java.io.File;

import org.bukkit.Bukkit;
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
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.World.BauWorld;
import net.wargearworld.Bau.World.WorldManager;
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
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		// p.teleport(new Location(Bukkit.getWorld("world"), 0, 30, 0));
		String lang = DBConnection.getLanguage(p);
		MessageHandler.playersLanguage.put(p.getUniqueId(), Language.valueOf(lang.toUpperCase()));
		// has own gs?
		String path = Bukkit.getWorldContainer().getAbsolutePath();
		File gs = new File(path + "/" + p.getUniqueId() + "_" + p.getName());
		System.out.println("gs exists: " + gs.exists());
		System.out.println("hasOWnPlot" + DBConnection.hasOwnPlots(p.getUniqueId().toString()));
		if (!DBConnection.hasOwnPlots(p.getUniqueId().toString()) && !gs.exists()) {
			// Bukkit.createWorld((WorldCreator) WorldCreator.name("test").createWorld());
			// wenn nicht-> erstellen und hinteleportieren
			String uuid = p.getUniqueId().toString();
			WorldManager.createWorldDir(p.getName(),uuid);
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "plotGenerating"));
		}
		World world = WorldManager.loadWorld(p.getName(),p.getUniqueId().toString());
		BauWorld bauWorld = WorldManager.get(world);
		bauWorld.spawn(p);
		// wenn ja-> teleportieren
		// item
		p.getInventory().setItem(0, ItemStackCreator.createNewItemStack(Material.NETHER_STAR, "§6GUI"));
		new ScoreBoardBau(p);

	}

}
