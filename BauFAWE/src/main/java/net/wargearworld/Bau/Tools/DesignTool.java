package net.wargearworld.Bau.Tools;

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

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;

public class DesignTool implements Listener, CommandExecutor {

	@EventHandler(priority = EventPriority.NORMAL)
	public void interactBlock(PlayerInteractEvent event) {
		if (event.getHand() == null) {
			return;
		}
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
		if (again.getType().equals(event.getMaterial()) || again.getType() == Material.BARRIER
				|| again.getRelative(event.getBlockFace()).isEmpty()) {
			return;
		}
		Player p = event.getPlayer();

		if (p.isSneaking()) {
			return;
		}
		if (!WorldGuardHandler.isInBuildRegion(again.getLocation()))
			return;

		BauPlayer player = BauPlayer.getBauPlayer(p);
		if (player.getDT()) {
			again.setType(event.getMaterial());
			event.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onblockPlace(BlockPlaceEvent event) {
		Block again = event.getBlockAgainst();
		if (again == null || event.getBlockPlaced().getType() == null) {
			return;
		}
		Player p = event.getPlayer();
		if (p.isSneaking())
			return;
		if (!WorldGuardHandler.isInBuildRegion(again.getLocation()) || again.getType() == Material.BARRIER)
			return;
		if (BauPlayer.getBauPlayer(p).getDT()) {
			again.setBlockData(event.getBlockPlaced().getBlockData());
			event.setCancelled(true);

		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String String, String[] args) {
		Player p = (Player) sender;
		BauPlayer player = BauPlayer.getBauPlayer(p);
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				dton(p);
				return true;
			} else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("aus")) {
				dtoff(p);
				return true;
			}
		} else if (args.length == 0) {
			if (player.getDT()) {
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
		BauPlayer player = BauPlayer.getBauPlayer(p);
		player.setDT(false);
		p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "dtOff"));
		ScoreBoardBau.cmdUpdate(p);
	}

	private void dton(Player p) {
		BauPlayer player = BauPlayer.getBauPlayer(p);
		player.setDT(true);
		p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "dtOn"));
		ScoreBoardBau.cmdUpdate(p);
	}
}
