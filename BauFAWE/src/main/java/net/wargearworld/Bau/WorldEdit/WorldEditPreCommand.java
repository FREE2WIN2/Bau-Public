package net.wargearworld.Bau.WorldEdit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.Tools.Stoplag;

public class WorldEditPreCommand implements Listener{

	@EventHandler
	public void PreCommandProcess(PlayerCommandPreprocessEvent event) {

		String command = event.getMessage();
		Player p = event.getPlayer();
		BauPlayer player = BauPlayer.getBauPlayer(p);
		if (command.startsWith("//paste")&&player.getPasteState()) {
			Stoplag.getInstance().setStatusTemp(p.getLocation(), true, player.getPasteTime());
		}

		if (command.equalsIgnoreCase("//rotate")) {
			event.setCancelled(true);
			WorldEditHandler.rotateClipboard(p);
		}
	}
}
