package net.wargearworld.bau.listener;

import net.wargearworld.StringGetter.Language;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.utils.ItemStackCreator;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.EntityManager;
import java.io.File;

public class onPlayerJoin implements Listener {

	public onPlayerJoin(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		// sprache
		Player p = e.getPlayer();
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		BauPlayer player = BauPlayer.getBauPlayer(p);
		String lang = player.getDbPlayer().getCountryCode();
		MessageHandler.playersLanguage.put(p.getUniqueId(), Language.valueOf(lang.toUpperCase()));
		// has own gs?
		String path = Bukkit.getWorldContainer().getAbsolutePath();
		File gs = new File(path + "/" + p.getUniqueId() + "_" + p.getName());
		EntityManager em = DependencyProvider.getEntityManager();
			String uuidString = p.getUniqueId().toString();
		if (!gs.exists()) {
			// Bukkit.createWorld((WorldCreator) WorldCreator.name("test").createWorld());
			// wenn nicht-> erstellen und hinteleportieren
			WorldManager.createWorldDir(p.getName(),uuidString,player.hasPlots());
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "plotGenerating"));
		}
		World world = WorldManager.loadWorld(p.getName(),uuidString);
		BauWorld bauWorld = WorldManager.get(world);
		bauWorld.spawn(p);
		// wenn ja-> teleportieren
		// item
		p.getInventory().setItem(0, ItemStackCreator.createNewItemStack(Material.NETHER_STAR, "§6GUI"));
		new ScoreBoardBau(p);

	}

}
