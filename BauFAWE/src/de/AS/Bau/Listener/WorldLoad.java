package de.AS.Bau.Listener;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.Main;

public class WorldLoad implements Listener {
	public WorldLoad(JavaPlugin plugin){

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		SpawnEvent.worldRemoveEntities.put(e.getWorld(), false);
		Main main = Main.getPlugin();
		if(!main.getCustomConfig().contains("stoplag."+e.getWorld().getName())){
			main.getCustomConfig().createSection("stoplag."+e.getWorld().getName()+".plot1");
			main.getCustomConfig().createSection("stoplag."+e.getWorld().getName()+".plot2");
			main.getCustomConfig().createSection("stoplag."+e.getWorld().getName()+".plot3");

		}
		main.getCustomConfig().set("stoplag."+e.getWorld().getName()+".plot1", "aus");
		main.getCustomConfig().set("stoplag."+e.getWorld().getName()+".plot2", "aus");
		main.getCustomConfig().set("stoplag."+e.getWorld().getName()+".plot3", "aus");
		try {
			main.getCustomConfig().save(main.getCustomConfigFile());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
