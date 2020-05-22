package de.AS.Bau.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;

public class WorldHandler {

	private static String templateName = Main.getPlugin().getCustomConfig().getString("plottemplate");
	
	public static World loadWorld(String worldName) {
		WorldCreator wc = new WorldCreator(worldName);
		wc.type(WorldType.NORMAL);
		World w = Bukkit.getServer().createWorld(wc);
		w.setStorm(false);
		w.setThundering(false);
		w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
//		WorldBorder b = w.getWorldBorder();
//		b.setCenter(-208.53, 17.23);
//		b.setSize(400);

		return w;
	}

	public static void unloadWorld(String worldName) {
		undloadWorld(Bukkit.getWorld(worldName));
	}

	public static void undloadWorld(World world) {
		if(world == null) {
			return;
		}
		Bukkit.unloadWorld(world, true);
	}

	public static void createWorldDir(Player p) {
		String uuid = p.getUniqueId().toString();
		File vorlage = new File(Bukkit.getWorldContainer(),templateName);
		// File neu = new File(path + "/Worlds/" + uuid);
		File neu = new File(Bukkit.getWorldContainer(),uuid);
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
		File worldGuardWorldDir = new File(Bukkit.getWorldContainer(),"plugins/WorldGuard/worlds/" + uuid);
		if (!worldGuardWorldDir.exists()) {
			File vorlageWorldGuardWorldDir = new File(Bukkit.getWorldContainer(), "plugins/WorldGuard/worlds/"+templateName);
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
				}
			}, 20);

		}
	}

	public static boolean deleteWorld(World w, DBConnection conn) {
		Bukkit.getServer().unloadWorld(w, true);
		if (w.getWorldFolder().exists()) {
			if (deleteDir(w.getWorldFolder()) && conn.deleteGs(w.getName())) {
				File file = new File(Bukkit.getWorldContainer(),"plugins/WorldGuard/worlds/" + w.getName());
				file.delete();
				return true;
			}
		}
		return false;

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
				Files.copy(sourceFolder.toPath(), destinationfolder.toPath(), StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static boolean deleteDir(File path) {
		if (path.exists()) {
			File files[] = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDir(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	
	public static void checkForWorldsToUnload() {
	Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				for(World w:Bukkit.getServer().getWorlds()) {
					if(w.getPlayers().size() == 0&&!w.getName().equals("world")) {
						undloadWorld(w);
					}
				}
				
			}
		}, 20*60, 20*60);
	}
}
