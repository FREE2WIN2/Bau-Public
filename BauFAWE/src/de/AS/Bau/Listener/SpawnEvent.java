package de.AS.Bau.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;

public class SpawnEvent implements Listener {

	public static HashMap<World,Boolean> worldRemoveEntities = new HashMap<>();
	public SpawnEvent(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
@SuppressWarnings("unlikely-arg-type")
@EventHandler
public void onEntitSpawn(EntitySpawnEvent e) {
	if(!worldRemoveEntities.containsKey(e.getLocation().getWorld())) {
		worldRemoveEntities.put(e.getLocation().getWorld(), false);
	}
	EntityType etype = e.getEntityType();
	if(worldRemoveEntities.get(e.getLocation().getWorld())){
		e.setCancelled(true);
		return;
	}
	if(etype.equals(EntityType.FALLING_BLOCK)) {
		if(!(etype.equals(EntityType.FALLING_BLOCK))&&!e.getEntity().isCustomNameVisible()){
			e.setCancelled(true);
		}else {
			if(e.getLocation().getWorld().getEntities().size() > 2000) {
				
				logeintrag(e.getLocation().getWorld(),e.getLocation().getWorld().getEntities().size());
				
				for(Entity en:e.getLocation().getWorld().getEntities()) {
					if(!en.getType().equals(EntityType.PLAYER)) {
						en.remove();
					}
				}
				for(Player p:e.getLocation().getWorld().getPlayers()) {
					p.sendMessage(Main.prefix +StringGetterBau.getString(p,"tooManyEntities"));
					//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick "+ p.getName());
				}
				worldRemoveEntities.put(e.getLocation().getWorld(), true);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
					
					@Override
					public void run() {
						worldRemoveEntities.put(e.getLocation().getWorld(), false);
					}
				}, 80);
			}
		}
	}else if(etype.equals(EntityType.PRIMED_TNT)||etype.equals(Material.FIRE_CHARGE)){
		//gucken ob in dern Nähe ungezündetes TNT ist
		if(e.getLocation().getWorld().getEntities().size() > 1000) {
			
			logeintrag(e.getLocation().getWorld(),e.getLocation().getWorld().getEntities().size());
			worldRemoveEntities.put(e.getLocation().getWorld(), true);
			
			for(Entity en:e.getLocation().getWorld().getEntities()) {
				if(!en.getType().equals(EntityType.PLAYER)) {
					en.remove();
				}
			}
			for(Player p:e.getLocation().getWorld().getPlayers()) {
				p.sendMessage(Main.prefix +StringGetterBau.getString(p,"tooManyEntities"));
				//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick "+ p.getName());
			}
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
				
				@Override
				public void run() {
					worldRemoveEntities.put(e.getLocation().getWorld(), false);
				}
			}, 200);
			//wann wieder false? ->
		}
	}else {
		e.setCancelled(true);
	}
}
private void logeintrag(World world, int size) {
	
	try {
		File f = new File(Main.getPlugin().getDataFolder().getAbsolutePath() + "/log.txt");
		if(!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}	
		FileWriter fw = new FileWriter(f,true);
		String time;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
		Date date = new Date();
		time = "["+formatter.format(date)+"]";
		DBConnection conn = new DBConnection();
		fw.write(time + " " + world.getName() + " " + size + "Entities Entfernt! ->"+conn.getName(world.getName())+"\n");
		fw.flush();
		fw.close();
		conn.closeConn();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
}
