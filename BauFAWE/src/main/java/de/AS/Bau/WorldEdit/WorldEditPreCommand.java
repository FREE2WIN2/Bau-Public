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
		if (command.startsWith("//paste")) {
			Stoplag.setStatusTemp(p.getLocation(), true, 5);
		}

		if (command.equalsIgnoreCase("//rotate")) {
			event.setCancelled(true);
			WorldEditHandler.rotateClipboard(p);
		}
	}
}
