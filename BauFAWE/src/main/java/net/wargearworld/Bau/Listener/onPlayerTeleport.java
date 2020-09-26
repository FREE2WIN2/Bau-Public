package net.wargearworld.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.World.BauWorld;
import net.wargearworld.Bau.World.WorldManager;

public class onPlayerTeleport implements Listener {
	public onPlayerTeleport(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		if (e.getTo().getWorld().getName().contains("test") && p.hasPermission("supporter")) {
			return;
		}
		if (e.getTo().getWorld() == e.getFrom().getWorld()) {
			ScoreBoardBau.cmdUpdate(p);
			return;
		}

		if (p.hasPermission("moderator")) {
			return;
		}
		BauWorld world = WorldManager.get(e.getTo().getWorld());
		if (!world.isAuthorized(p.getUniqueId())) {
			// wenn er nicht owner und nicht Member ist
			e.setCancelled(true);
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "noPlotMember"));
		}
	}

}