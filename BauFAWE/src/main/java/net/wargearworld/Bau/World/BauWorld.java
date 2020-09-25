package net.wargearworld.Bau.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.HikariCP.DBConnection;

public class BauWorld {
	private UUID worldUUID;

	private int id;
	private HashMap<String,Plot> plots;
	private RegionManager regionManager;
	private WorldTemplate template;
	
	private File configFile;
	private FileConfiguration config;
	public BauWorld(int id,String owner, World world) {
		this.worldUUID = world.getUID();
		this.id = id;

		regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
		String templateName = DBConnection.getTemplate(id);
		template = WorldTemplate.getTemplate(templateName);
		
		plots = new HashMap<>();
		for(String plotID:template.getPlotIDs()) {
			Plot plot = createPlot(regionManager.getRegion(plotID),plotID);
			plots.put(plotID, plot);
		}
		
		configFile = new File(Main.getPlugin().getDataFolder(),"worlds/" + id + "/settings.yml");
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	

	public Plot getPlot(String plotID) {
		return plots.get(plotID);
	}

	public Plot getPlot(Location loc) {
		BlockVector3 pos = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
		return getPlot(regionManager.getApplicableRegionsIDs(pos).get(0));
	}
	
	public World getWorld() {
		return Bukkit.getWorld(worldUUID);
	}

	public RegionManager getRegionManager() {
		return regionManager;
	}
	
	/*FactoryMethod for PLot*/
	private Plot createPlot(ProtectedRegion region, String plotID) {
		switch(template.getType(plotID)) {
		case DEFAULT:
			return new DefaultPlot(region, plotID);
		case TEST:
			return new TestPlot(region, plotID);
		}
		return null;
	}


	
}
