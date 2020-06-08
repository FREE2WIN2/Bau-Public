package net.wargearworld.Bau.cmds;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.wargearworld.Bau.Main;

public class clear implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				ItemStack chestplate = new ItemStack(Material.AIR);
				if (p.getInventory().getChestplate() != null) {
					chestplate = p.getInventory().getChestplate();
				}
				p.getInventory().clear();
				p.getInventory().setChestplate(chestplate);
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
