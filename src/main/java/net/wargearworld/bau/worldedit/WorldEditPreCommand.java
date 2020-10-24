package net.wargearworld.bau.worldedit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.Stoplag;

public class WorldEditPreCommand implements Listener{

	@EventHandler
	public void PreCommandProcess(PlayerCommandPreprocessEvent event) {

		String command = event.getMessage();
		Player p = event.getPlayer();
		BauPlayer player = BauPlayer.getBauPlayer(p);
		if ((command.startsWith("//paste")||(command.startsWith("//move"))||(command.startsWith("//stack")))&&player.getPasteState()) {
			Stoplag.getInstance().setStatusTemp(p.getLocation(), true, player.getPasteTime());
		}

		if (command.equalsIgnoreCase("//rotate")) {
			event.setCancelled(true);
			WorldEditHandler.rotateClipboard(p);
		}
	}
}
