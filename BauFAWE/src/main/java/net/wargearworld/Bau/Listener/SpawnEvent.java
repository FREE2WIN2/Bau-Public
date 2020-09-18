package net.wargearworld.Bau.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.StringGetterBau;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.utils.HelperMethods;

public class SpawnEvent implements Listener {

	public static Set<UUID> worldSpawnEntitiesBlocked = new HashSet<>();

	public SpawnEvent(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@SuppressWarnings("unlikely-arg-type")
	@EventHandler
	public void onEntitSpawn(EntitySpawnEvent e) {
		EntityType etype = e.getEntityType();
		if (worldSpawnEntitiesBlocked.contains(e.getLocation().getWorld().getUID())) {
			e.setCancelled(true);
			return;
		}
		if(e.getLocation().getWorld().getName().contains("test"))
			return;
		if (etype.equals(EntityType.FALLING_BLOCK)) {
			if (!etype.equals(EntityType.FALLING_BLOCK) && !e.getEntity().isCustomNameVisible()) {
				e.setCancelled(true);
			} else {
				if (e.getLocation().getWorld().getEntities().size() > 2000) {

					logeintrag(e.getLocation().getWorld(), e.getLocation().getWorld().getEntities().size());

					for (Entity en : e.getLocation().getWorld().getEntities()) {
						if (!en.getType().equals(EntityType.PLAYER)) {
							en.remove();
						}
					}
					for (Player p : e.getLocation().getWorld().getPlayers()) {
						p.sendMessage(Main.prefix + StringGetterBau.getString(p, "tooManyEntities"));
					}
					worldSpawnEntitiesBlocked.add(e.getLocation().getWorld().getUID());
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

						@Override
						public void run() {
							worldSpawnEntitiesBlocked.remove(e.getLocation().getWorld().getUID());
						}
					}, 80);
				}
			}
		} else if (etype.equals(EntityType.PRIMED_TNT) || etype.equals(Material.FIRE_CHARGE)) {
			// gucken ob in dern Nähe ungezündetes TNT ist
			if (e.getLocation().getWorld().getEntities().size() > 1000) {

				logeintrag(e.getLocation().getWorld(), e.getLocation().getWorld().getEntities().size());
				worldSpawnEntitiesBlocked.add(e.getLocation().getWorld().getUID());

				for (Entity en : e.getLocation().getWorld().getEntities()) {
					if (!en.getType().equals(EntityType.PLAYER)) {
						en.remove();
					}
				}
				for (Player p : e.getLocation().getWorld().getPlayers()) {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "tooManyEntities"));
					// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick "+ p.getName());
				}
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						worldSpawnEntitiesBlocked.remove(e.getLocation().getWorld().getUID());
					}
				}, 200);
				// wann wieder false? ->
			}
		} else {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void VehicleSpawn(VehicleCreateEvent event) {
		event.setCancelled(true);
		
	}
	
	private void logeintrag(World world, int size) {

		try {
			File f = new File(Main.getPlugin().getDataFolder().getAbsolutePath() + "/log.txt");
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f, true);
			String time = HelperMethods.getTime();
			fw.write(time + " " + world.getName() + " " + size + "Entities Entfernt! ->"
					+ DBConnection.getName(world.getName()) + "\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
