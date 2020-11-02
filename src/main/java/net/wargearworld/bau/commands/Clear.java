package net.wargearworld.bau.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.wargearworld.bau.Main;

public class Clear implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				p.getInventory().clear();
				ItemStack guiItem = new ItemStack(Material.NETHER_STAR);
				ItemMeta guiMeta = guiItem.getItemMeta();
				guiMeta.setDisplayName("§6GUI");
				guiItem.setItemMeta(guiMeta);
				p.getInventory().setItem(0, guiItem);
				Main.send(p, "inv_cleared");
			}
		}
		return false;
	}
}
