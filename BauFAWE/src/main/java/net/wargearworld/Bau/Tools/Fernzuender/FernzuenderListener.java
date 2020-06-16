package net.wargearworld.Bau.Tools.Fernzuender;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.StringGetterBau;

public class FernzuenderListener implements Listener {
	public static HashMap<UUID, Fernzuender> playersDetonator = new HashMap<>();
	public static Material toolMaterial = Material.valueOf(Main.getPlugin().getCustomConfig().getString("fernzuender"));
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack is = event.getItem();
		if (!(is == null)) {
			if (is.getType().equals(toolMaterial)) {
				fernzuender(event);

			}
		}
	}

	private void fernzuender(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Action a = e.getAction();
		Fernzuender detonator = getFZ(p.getUniqueId());
		if (a.equals(Action.RIGHT_CLICK_AIR)||a.equals(Action.LEFT_CLICK_AIR)) {
			if (detonator.getLoc() != null) {
				detonator.activate();
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "nothingSaved"));
			}
		} else if (a.equals(Action.RIGHT_CLICK_BLOCK) || a.equals(Action.LEFT_CLICK_BLOCK)) {
			// speichern oder zündeln!
			Block b = e.getClickedBlock();
			if (b.getBlockData() instanceof Switch||b.getBlockData() instanceof NoteBlock) {
				detonator.setLoc(b.getLocation());
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzSaved"));
				e.setCancelled(true);
			} else {
				if (detonator.getLoc() != null) {
					detonator.activate();
					e.setCancelled(true);
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "nothingSaved"));
				}
			}
		}

	}

	public static Fernzuender getFZ(UUID uuid) {
		if(!playersDetonator.containsKey(uuid)) {
			playersDetonator.put(uuid, new Fernzuender(uuid));
		}
		return playersDetonator.get(uuid);
	}


}
