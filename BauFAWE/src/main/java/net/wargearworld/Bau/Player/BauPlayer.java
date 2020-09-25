package net.wargearworld.Bau.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.HikariCP.DBConnection;



public class BauPlayer {
	private static HashMap<UUID,BauPlayer> players = new HashMap<>();
	public static BauPlayer getBauPlayer(Player p) {
		if(p!= null)
			return getBauPlayer(p.getUniqueId());
		return null;
	}
	public static BauPlayer getBauPlayer(UUID uuid) {
		BauPlayer player = players.get(uuid);
		if(player == null) {
			player = new BauPlayer(uuid);
			players.put(uuid, player);
		}
		return player;
	}
	
	private UUID uuid; 
	FileConfiguration config;
	File configFile;
	
	
	private BauPlayer(UUID uuid) {
		this.uuid = uuid;
		configFile = new File(Main.getPlugin().getDataFolder(),"users/" + uuid.toString() + "/settings.yml");
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public Player getBukkitPlayer() {
		return Bukkit.getPlayer(uuid);
	}
	public String getName() {
		Player p = getBukkitPlayer();
		if (p == null) {
			return DBConnection.getName(getUuid().toString());
		}
		return p.getName();
	}
	private void save() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* Stoplag */
	
	public void setSLPaste(boolean active) {
		config.set("stoplag.paste", active);
		save();
	}
	public void setSLPasteTime(Integer time) {
		config.set("stoplag.pastetime", time);
		save();
	}

	public boolean getPasteState() {
		return config.getBoolean("stoplag.paste");
	}

	public int getPasteTime() {
		return config.getInt("stoplag.pastetime");
	}
}
