package net.wargearworld.Bau.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerUtil {

	
	public static boolean isOnline(UUID uuid){
		return Bukkit.getPlayer(uuid) !=null;
	}
	
	public static Player getPlayer(UUID uuid) {
		if(!isOnline(uuid)) {
			return null;
		}
		return Bukkit.getPlayer(uuid);
	}
}
