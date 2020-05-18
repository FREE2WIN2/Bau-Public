package de.AS.Bau.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Scoreboard.ScoreBoardBau;
import de.AS.Bau.cmds.dt;

public class onPlayerJoin implements Listener {


	public onPlayerJoin(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		// sprache
		Player p = e.getPlayer();
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		//p.teleport(new Location(Bukkit.getWorld("world"), 0, 30, 0));
		DBConnection conn = new DBConnection();
		String lang = conn.getLanguage(p);
		StringGetterBau.playersLanguage.put(p.getUniqueId(), lang);
		Main main = Main.getPlugin();
		// has own gs?
		String path = main.getCustomConfig().getString("Config.path");
		File gs =new File(path + "/" + p.getUniqueId().toString());
		if (!conn.hasOwnPlots(p)&&!gs.exists()) {
			// Bukkit.createWorld((WorldCreator) WorldCreator.name("test").createWorld());
			// wenn nicht-> erstellen und hinteleportieren
			firstJoin(p);
			p.sendMessage(Main.prefix + StringGetterBau.getString(p,"plotGenerating"));
		}
		World world = loadWorld(p.getUniqueId().toString());

		dt.playerHasDtOn.put(p.getUniqueId(), false);
		Location loc = new Location(world, -208, 8, 17);
		onPlayerMove.playersLastPlot.put(p, "plot2");

		if (!main.getCustomConfig().contains("stoplag." + p.getUniqueId().toString())) {
			main.getCustomConfig().createSection("stoplag." + p.getUniqueId().toString() + ".plot1");
			main.getCustomConfig().createSection("stoplag." + p.getUniqueId().toString() + ".plot2");
			main.getCustomConfig().createSection("stoplag." + p.getUniqueId().toString() + ".plot3");
			main.getCustomConfig().set("stoplag." + p.getUniqueId().toString() + ".plot1", "aus");
			main.getCustomConfig().set("stoplag." + p.getUniqueId().toString() + ".plot2", "aus");
			main.getCustomConfig().set("stoplag." + p.getUniqueId().toString() + ".plot3", "aus");
			try {
				main.getCustomConfig().save(main.getCustomConfigFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		p.teleport(loc);
		// wenn ja-> teleportieren
		// item
		ItemStack guiItem = new ItemStack(Material.NETHER_STAR);
		ItemMeta guiMeta = guiItem.getItemMeta();
		guiMeta.setDisplayName("§6GUI");
		guiItem.setItemMeta(guiMeta);
		p.getInventory().setItem(0, guiItem);
		new ScoreBoardBau(p);
		conn.closeConn();

	}

	public void firstJoin(Player p) {
		String uuid = p.getUniqueId().toString();
		Main main = Main.getPlugin();
		String path = main.getCustomConfig().getString("Config.path");
		File vorlage = new File(path + "/BauGsVorlage");
		//File neu = new File(path + "/Worlds/" + uuid);
		File neu = new File(path + "/" + uuid);
		neu.mkdirs();
		neu.setExecutable(true, false);
		neu.setReadable(true, false);
		neu.setWritable(true, false);
		copyFolder_raw(vorlage, neu);

		DBConnection conn = new DBConnection();
		if (!conn.hasOwnPlots(p)) {
			conn.registerNewPlot(p);
		}
		conn.closeConn();
		// worldguard regionen
		File worldGuardWorldDir = new File(path + "/plugins/WorldGuard/worlds/" + uuid);
		if (worldGuardWorldDir.exists()) {
			return;
		} else {
			File vorlageWorldGuardWorldDir = new File(path + "/plugins/WorldGuard/worlds/BauGsVorlage");
			copyFolder_raw(vorlageWorldGuardWorldDir, worldGuardWorldDir);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

				@Override
				public void run() {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							"rg addowner -w \"" + uuid + "\" plot1 " + p.getUniqueId().toString());
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							"rg addowner -w \"" + uuid + "\" plot2 " + p.getUniqueId().toString());
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							"rg addowner -w \"" + uuid + "\" plot3 " + p.getUniqueId().toString());
					System.out.println("regionsadd");
				}
			}, 20);

		}
	}

	public static World loadWorld(String worldName) {
		WorldCreator wc = new WorldCreator(worldName);
		wc.type(WorldType.NORMAL);
		World w = Bukkit.getServer().createWorld(wc);
		w.setStorm(false);
		w.setThundering(false);
		w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		WorldBorder b = w.getWorldBorder();
		b.setCenter(-208.53, 17.23);
		b.setSize(400);

		return w;
	}

	public static void copyFolder_raw(File sourceFolder, File destinationfolder) {
		// Check if sourceFolder is a directory or file
		// If sourceFolder is file; then copy the file directly to new location
		if (sourceFolder.isDirectory()) {
			// Verify if destinationFolder is already present, if not then create it
			if (!destinationfolder.exists()) {
				destinationfolder.mkdir();
			}
			// Get all files from source directory
			String files[] = sourceFolder.list();
			// Iterate over all files and copy them to destinationFolder one by one
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationfolder, file);
				// Recursive function call
				copyFolder_raw(srcFile, destFile);
			}
		} else {
			// Copy the file content from one place to another
			try {
					Files.copy(sourceFolder.toPath(), destinationfolder.toPath(), StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
