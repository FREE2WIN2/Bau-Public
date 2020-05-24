package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ClickListener implements Listener {
	public ClickListener(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onclick(PlayerInteractEvent e) {
		
		Player p = (Player) e.getPlayer();
		ItemStack is = e.getItem();
		if (!(is == null)) {
			if (is.getItemMeta().getDisplayName().equals("�6GUI") && (e.getAction().equals(Action.RIGHT_CLICK_AIR)
					|| e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
				p.performCommand("gui");
			} else if (is.getType().equals(Material.SPAWNER)) {
				e.setCancelled(true);
			}
		}
	}

}
