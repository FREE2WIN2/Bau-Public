package de.AS.Bau.Plots;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.AS.Bau.Main;
import de.AS.Bau.HikariCP.DBConnection;

public class Plots {

	private static HashMap<String,YamlConfiguration> gsConfigs;
	private static HashMap<UUID,UsersPlot> loadedWorlds;
	
	public Plots() {
		
		loadedWorlds = new HashMap<>();
		gsConfigs = new HashMap<>();
		File configDir = new File(Main.getPlugin().getDataFolder(),"worldConfigs");
		for(File config:configDir.listFiles()) {
			YamlConfiguration configyaml = new YamlConfiguration();
			String key = config.getName().replace(".yml", "");
			try {
				configyaml.load(config);
				gsConfigs.put(key, configyaml);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean addPlot(UUID ownerUUID) {
		if(loadedWorlds.containsKey(ownerUUID)) {
			return false;
		}
		UsersPlot plot = new UsersPlot(ownerUUID, DBConnection.getTemplate(ownerUUID),DBConnection.getMember(ownerUUID.toString()));
		loadedWorlds.put(ownerUUID, plot);
		return true;
	}
	
	public static void removeWorld(UUID worldUUID) {
		loadedWorlds.remove(worldUUID);
	}
	
	public static YamlConfiguration getConfig(String template) {
		return gsConfigs.get(template);
	}
	
	public static UsersPlot getPlot(UUID owner) {
		if(!loadedWorlds.containsKey(owner)) {
			addPlot(owner);
		}
		return loadedWorlds.get(owner);
	}

	public static String getJoinPlot(UUID uniqueId) {
		return getConfigOfPlot(uniqueId).getString("coordinates.spawn");
	}
	public static YamlConfiguration getConfigOfPlot(UUID ownerUUID) {
		String template = getPlot(ownerUUID).getTemplate();
		return gsConfigs.get(template);
	}
	public static YamlConfiguration getConfigOfPlot(String worldName) {
		if(worldName)
		String template = getPlot(ownerUUID).getTemplate();
		return gsConfigs.get(template);
	}
}
