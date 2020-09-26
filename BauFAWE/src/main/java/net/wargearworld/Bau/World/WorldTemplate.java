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
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import net.wargearworld.Bau.WorldEdit.Schematic;
import net.wargearworld.Bau.utils.Loc;

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
	
	private List<PlotPattern> plots;
	private String spawnPlotID;

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
	
	public List<PlotPattern> getPlots(){
		if(plots == null) {
			plots = new ArrayList<>();
			for(String id:config.getConfigurationSection("plots").getValues(false).keySet()) {
				Loc middleNorth = Loc.getByString(config.getString("middle." + id));
				PlotType type = getType(id);
				Schematic ground = new Schematic(Main.schempath + "/TestBlockSklave", config.getString("plotreset.schemfiles." + id), Facing.NORTH);
				plots.add(new PlotPattern(middleNorth, ground, type, id));
			}
		}
		return plots;
	}
	
	public String getSpawnPlotID() {
		if(spawnPlotID == null) {
			spawnPlotID = config.getString("spawn");
		}
		return spawnPlotID;
	}
	
//	public PlotType getType(Plot plot) {
//		return PlotType.valueOf(config.getString("plots." + plot.getId()).toUpperCase());
//	}

	public PlotType getType(String plotID) {
		return PlotType.valueOf(config.getString("plots." + plotID).toUpperCase());
	}

}
