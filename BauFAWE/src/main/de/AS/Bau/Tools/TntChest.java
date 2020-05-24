package de.AS.Bau.Tools;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TntChest implements CommandExecutor, Listener {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack is = new ItemStack(Material.CHEST);
			ItemMeta im = is.getItemMeta();
			im.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, true);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			is.setItemMeta(im);
			p.getEquipment().setItemInMainHand(is);
		}
		return true;
	}

	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onblockPlace(BlockPlaceEvent e) {
		if (e.getItemInHand().hasItemMeta() && !e.getItemInHand().getEnchantments().isEmpty()
				&& e.getItemInHand().getType().equals(Material.CHEST)) {
			Chest chest = (Chest) e.getBlock().getState();
			ItemStack tnt = new ItemStack(Material.TNT);
			tnt.setAmount(64);
			for (int i = 0; i < 27; i++) {
				chest.getInventory().setItem(i, tnt);
			}
		}
	}
}
