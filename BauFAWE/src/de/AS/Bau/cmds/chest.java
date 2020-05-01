package de.AS.Bau.cmds;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class chest implements Listener, CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack is = new ItemStack(Material.CHEST);
			ItemMeta im = is.getItemMeta();
			im.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, true);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			is.setItemMeta(im);
			p.getInventory().setItemInHand(is);
		}
		return true;
	}

}
