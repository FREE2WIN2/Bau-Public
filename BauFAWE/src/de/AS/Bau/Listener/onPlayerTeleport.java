package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.HikariCP.DBConnection;

public class onPlayerTeleport implements Listener {
	public onPlayerTeleport(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		if(e.getTo().getWorld().getName().contains("test")&& p.hasPermission("supporter")){
			return;
		}
		
		if (!p.hasPermission("moderator")) {
			if (!e.getTo().getWorld().getName().equals(p.getUniqueId().toString())&&!p.getWorld().getName().equals("world")) {
				// wenn er nicht owner ist
				if (!DBConnection.isMember(p.getUniqueId(), DBConnection.getName(e.getTo().getWorld().getName()))) {
					// wenn er nicht owner und nicht Member ist
					e.setCancelled(true);
					p.sendMessage(Main.prefix +StringGetterBau.getString(p,"noPlotMember"));
				}
			}
		}
	}

}