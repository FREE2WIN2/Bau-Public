package net.wargearworld.Bau.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Plots.Plots;
import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;

public class WorldManager {

	private static HashMap<UUID,BauWorld> worlds = new HashMap<>();
	
	public static BauWorld get(World world) {
		return worlds.get(world.getUID());
	}
	
	public static String templateName = Main.getPlugin().getCustomConfig().getString("plottemplate");

	public static World loadWorld(String worldName) {
		if (Bukkit.getWorld(worldName) == null) {
			WorldCreator wc = new WorldCreator(worldName);
			wc.type(WorldType.NORMAL);
			World w = Bukkit.getServer().createWorld(wc);
			w.setStorm(false);
			w.setThundering(false);
			w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
			w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			
//			worlds.put(w.getUID(), new BauWorld(id, owner, world))
			return w;
		} else {
			return Bukkit.getWorld(worldName);
		}
	}

	public static void unloadWorld(String worldName) {
		undloadWorld(Bukkit.getWorld(worldName));
	}

	public static void undloadWorld(World world) {
		if (world == null) {
			return;
		}
		Bukkit.unloadWorld(world, true);
		if (!world.getName().contains("test") && !world.getName().contains("world")) {
			Plots.removeWorld(UUID.fromString(world.getName()));
		}
	}

	public static void createWorldDir(Player p) {
		String uuid = p.getUniqueId().toString();
		File vorlage = new File(Bukkit.getWorldContainer(), templateName);
		// File neu = new File(path + "/Worlds/" + uuid);
		File neu = new File(Bukkit.getWorldContainer(), uuid);
		neu.mkdirs();
		neu.setExecutable(true, false);
		neu.setReadable(true, false);
		neu.setWritable(true, false);
		copyFolder_raw(vorlage, neu);
		DBConnection.registerNewPlot(p.getUniqueId());
		// worldguard regionen
		File worldGuardWorldDir = new File(Bukkit.getWorldContainer(), "plugins/WorldGuard/worlds/" + uuid);
		File vorlageWorldGuardWorldDir = new File(Bukkit.getWorldContainer(),
				"plugins/WorldGuard/worlds/" + templateName);
		copyFolder_raw(vorlageWorldGuardWorldDir, worldGuardWorldDir);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				WorldGuardHandler.addOwnerToAllRegions(p.getUniqueId());
			}
		}, 20);

	}

	public static boolean deleteWorld(World w) {
		Bukkit.getServer().unloadWorld(w, true);
		if (!w.getName().contains("test") && !w.getName().contains("world")) {
			Plots.removeWorld(UUID.fromString(w.getName()));
		}
		if (w.getWorldFolder().exists()) {
			if (deleteDir(w.getWorldFolder()) && DBConnection.deleteGs(w.getName())) {
				File file = new File(Bukkit.getWorldContainer(), "plugins/WorldGuard/worlds/" + w.getName());
				file.delete();
				return true;
			}
		}
		return false;

	}

	public static void copyFolder_raw(File sourceFolder, File destinationfolder) {
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
				for (World w : Bukkit.getServer().getWorlds()) {
					if (w.getPlayers().size() == 0 && !w.getName().equals("world")) {
						undloadWorld(w);
					}
				}

			}
		}, 20 * 60, 20 * 60);
	}

	

}
