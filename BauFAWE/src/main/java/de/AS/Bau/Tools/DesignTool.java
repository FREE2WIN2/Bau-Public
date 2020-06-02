package de.AS.Bau.Tools;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Scoreboard.ScoreBoardBau;

public class DesignTool implements Listener, CommandExecutor {
	public static HashMap<UUID, Boolean> playerHasDtOn = new HashMap<>();

	@EventHandler(priority = EventPriority.NORMAL)
	public void interactBlock(PlayerInteractEvent event) {
		if (!event.getHand().equals(EquipmentSlot.HAND)) {
			return;
		}
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		Block again = event.getClickedBlock();
		if (again == null || event.getMaterial() == null) {
			return;
		}
		if(again.getType().equals(event.getMaterial())) {
			return;
		}
		if (playerHasDtOn.get(event.getPlayer().getUniqueId()) == true && again != null) {
			if ((again.getY() < 69 && again.getY() > 8) || again.getType().equals(Material.BARRIER)) {
				if (!event.getPlayer().isSneaking()) {
					again.setType(event.getMaterial());
					event.setCancelled(true);
				}

			}
		}

	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onblockPlace(BlockPlaceEvent event) {
		Block again = event.getBlockAgainst();
		if (again == null || event.getBlockPlaced().getType() == null) {
			return;
		}

		if (playerHasDtOn.get(event.getPlayer().getUniqueId()) == true && again != null) {
			if ((again.getY() < 69 && again.getY() > 8) || again.getType().equals(Material.BARRIER)) {
				if (!event.getPlayer().isSneaking()) {
					again.setBlockData(event.getBlockPlaced().getBlockData());
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
				dtoff(p);
				return true;
			} else {
				dton(p);
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
