  package net.wargearworld.Bau.World;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.wargearworld.Bau.Main;

public class WorldTemplate {
	private static HashMap<String,WorldTemplate> templates;
	public static void load() {
		File dir = new File(Main.getPlugin().getDataFolder(),"worldConfigs");
		for(File file: dir.listFiles()) {
			WorldTemplate template = new WorldTemplate(file);
			templates.put(template.getName(), template);
		}
	}
	
	public static WorldTemplate getTemplate(String name) {
		if(templates == null)
			load();
		return templates.get(name);
	}
	
	
	private File configFile;
	private FileConfiguration config;
	private String name;
	
	List<String> plotIDs;
	private WorldTemplate(File configFile) {
		name = configFile.getName().split("\\.")[0];
		this.configFile = configFile;
		config = new YamlConfiguration();
		try {
			config.load(this.configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return name;
	}
	public FileConfiguration getConfig() {
		return config;
	}
	
	public List<String> getPlotIDs(){
		if(plotIDs == null) {
			plotIDs = new ArrayList<String>();
			for(String id:config.getConfigurationSection("plots").getValues(false).keySet()) {
				plotIDs.add(id);
			}
		}
		return plotIDs;
	}
	
	public PlotType getType(Plot plot) {
		return PlotType.valueOf(config.getString("plots." + plot.getId()).toUpperCase());
	}

	public PlotType getType(String plotID) {
		return PlotType.valueOf(config.getString("plots." + plotID).toUpperCase());
	}
}
