package de.AS.Bau.WorldEdit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.AS.Bau.Main;

public class SaWE implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void playerCommandHandler(PlayerCommandPreprocessEvent event) {
		WorldEdit we = WorldEdit.getInstance();
		String command = event.getMessage().split(" ")[0];
		if (!isWorldEditCommand(command)) {
			return;
		}
		Player p = event.getPlayer();
		if (p.hasPermission("bau.build.bypass")) {
			return;
		}
		changeMask(p, p.getLocation());
		if (we.getSessionManager().get(BukkitAdapter.adapt(p)).getMask() != null) {
			Main.send(p, "noWE");
			event.setCancelled(true);
		}
	}

	private void changeMask(Player player, Location to) {
		WorldEdit we = WorldEdit.getInstance();
		LocalSession session = we.getSessionManager().get(BukkitAdapter.adapt(player));
		if (player.hasPermission("bau.build.bypass")) {
			session.setMask(null);
			return;
		}
		ProtectedRegion rg = WorldGuardHandler.getRegion(to);
		session.setMask(null);

		if (rg == null) {
			return;
		}

		RegionMask rm = getRegionMask(rg);
		session.setMask(rm);
	}

	private boolean isWorldEditCommand(String command) {
		if (command.startsWith("/")) {
			command = command.replaceFirst("/", "");
		}
		command = command.toLowerCase();
		return WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getCommandManager()
				.getCommand(command).isPresent();
	}

	private RegionMask getRegionMask(ProtectedRegion rg) {
		CuboidRegion cr = new CuboidRegion(rg.getMinimumPoint(), rg.getMaximumPoint());
		return new RegionMask(cr);
	}
}
