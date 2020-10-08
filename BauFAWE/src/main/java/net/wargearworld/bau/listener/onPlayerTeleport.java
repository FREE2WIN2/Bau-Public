package net.wargearworld.bau.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.WorldManager;

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