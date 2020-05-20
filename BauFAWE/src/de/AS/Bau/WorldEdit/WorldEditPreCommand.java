package de.AS.Bau.WorldEdit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.AS.Bau.Tools.Stoplag;

public class WorldEditPreCommand implements Listener{

	@EventHandler
	public void PreCommandProcess(PlayerCommandPreprocessEvent event) {

		String command = event.getMessage();
		Player p = event.getPlayer();
		String[] args = command.split(" ");
		if (command.startsWith("//paste")) {
//			boolean air = false;
//			if (command.contains(" -a") && args.length >= 2) {
//				air = true;
//			}
//			event.setCancelled(true);
//			BukkitPlayer player = BukkitAdapter.adapt(p);
//			LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
//			player.getLocation().getBlockX();
//			try {
//				WorldEditHandler.pasteAsync(session.getClipboard(), player.getLocation().getBlockX(),
//						player.getLocation().getBlockY(), player.getLocation().getBlockZ(), p, air, 1, true, false);
//			} catch (EmptyClipboardException e) {
//				e.printStackTrace();
//			}
			
			Stoplag.setStatusTemp(p.getLocation(), true, 5);

		}

		if (command.equalsIgnoreCase("//rotate")) {
			event.setCancelled(true);
			WorldEditHandler.rotateClipboard(p);
		}
	}
}
