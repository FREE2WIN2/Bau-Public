package de.AS.Bau.Tools;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Scoreboard.ScoreBoardBau;

public class DesignTool implements Listener, CommandExecutor {
	public static HashMap<UUID, Boolean> playerHasDtOn = new HashMap<>();

	@EventHandler
	public void placeBlock(BlockPlaceEvent event) {
		Block again = event.getBlockAgainst();
		Block placedBlock = event.getBlockPlaced();
		if (playerHasDtOn.get(event.getPlayer().getUniqueId()) == true && again != null) {
			if ((-65 > again.getX() && again.getX() > -371) || (again.getY() < 69 && again.getY() > 8)
					|| (again.getZ() < 99 && again.getZ() > -65)) {
				if (!event.getPlayer().isSneaking()) {
					again.setBlockData(placedBlock.getBlockData());
					event.setCancelled(true);
				}

			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String String, String[] args) {
		Player p = (Player) sender;
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				dton(p);
				return true;
			} else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("aus")) {
				dtoff(p);
				return true;
			}
		} else if (args.length == 0) {
			if (playerHasDtOn.get(p.getUniqueId())) {
				dton(p);
				return true;
			} else {
				dtoff(p);
				return true;
			}
		}
		return false;
	}

	private void dtoff(Player p) {
		playerHasDtOn.put(p.getUniqueId(), false);
		p.sendMessage(Main.prefix + StringGetterBau.getString(p, "dtOff"));
		ScoreBoardBau.cmdUpdate(p);
	}

	private void dton(Player p) {
		playerHasDtOn.put(p.getUniqueId(), true);
		p.sendMessage(Main.prefix + StringGetterBau.getString(p, "dtOn"));
		ScoreBoardBau.cmdUpdate(p);
	}
}
