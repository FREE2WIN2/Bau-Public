package de.AS.Bau.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;


import de.AS.Bau.Main;

public class worldCheck {
	public worldCheck() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				for(World w:Bukkit.getServer().getWorlds()) {
					if(w.getPlayers().size() == 0&&!w.getName().equals("world")) {
						unloadWorld(w.getName());
						
					}
				}
				
			}
		}, 12000, 12000);
	}
	public void unloadWorld(String worldName) {
		Bukkit.getServer().unloadWorld(worldName, true);
		Bukkit.getServer().getWorlds().remove(Bukkit.getServer().getWorld(worldName));
	}

}
