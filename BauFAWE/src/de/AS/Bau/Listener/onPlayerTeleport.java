package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;


import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;

public class onPlayerTeleport implements Listener {
	public onPlayerTeleport(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		if(e.getTo().getWorld().getName().contains("test")){
			return;
		}
		Player p = e.getPlayer();
		if (!p.hasPermission("moderator")) {
			if (!e.getTo().getWorld().getName().equals(p.getUniqueId().toString())&&!p.getWorld().getName().equals("world")) {
				// wenn er nicht owner ist
				DBConnection conn = new DBConnection();
				if (!conn.isMember(p, conn.getName(e.getTo().getWorld().getName()))) {
					// wenn er nicht owner und nicht Member ist
					e.setCancelled(true);
					p.sendMessage(Main.prefix +StringGetterBau.getString(p,"noPlotMember"));
				}
				conn.closeConn();
			}
		}
	}

}