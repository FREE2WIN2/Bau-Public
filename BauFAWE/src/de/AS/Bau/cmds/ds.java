package de.AS.Bau.cmds;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ds implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			p.setItemInHand(new ItemStack(Material.DEBUG_STICK));
		}
		return true;
	}

}
