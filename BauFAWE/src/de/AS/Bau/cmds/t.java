package de.AS.Bau.cmds;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Listener.OnInvClick;
import de.AS.Bau.utils.Banner;

public class t implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmds, String string, String[] args) {
		Player p = (Player) sender;
		if (args.length == 0) {
			p.openInventory(inventarErstellen(p));
			return true;
		} else if (args.length == 1) {
			if (args[1].equalsIgnoreCase("last")) {
				if (OnInvClick.playerLastPaste.containsKey(p)) {
					RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
					RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
					String rgID = regions.getApplicableRegionsIDs(
							BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
							.get(0);
					OnInvClick.pasten(OnInvClick.playerLastPaste.get(p), rgID, p);
					return true;
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noLastPaste"));
					return true;
				}
			}else {
				return false;
			}
		} else if (args.length == 3) {
			// /tbs <Tier> <Richtung> <normal?>
			// /tbs 1 n n
			String tier = "T";
			String Richtung = "";
			String typ = "";
			if (args[0].equals("1") || args[0].equals("2") || args[0].equals("3") || args[0].equals("4")) {
				if (args[1].equals("4")) {
					tier = "T3";
				} else {
					tier = tier + args[0];
				}
				if (args[1].equalsIgnoreCase("n") || args[1].equalsIgnoreCase("s")) {
					Richtung = args[1].toUpperCase();
				} else {
					return false;
				}
				if (args[2].equalsIgnoreCase("N") || args[2].equalsIgnoreCase("S") || args[2].equalsIgnoreCase("R")
						|| args[2].equalsIgnoreCase("F")) {
					if (args[2].equalsIgnoreCase("R")) {
						typ = "F";
					} else {
						typ = args[2].toUpperCase();
					}
				} else {
					return false;
				}
				String auswahl = tier + "_" + Richtung + "_" + typ;
				RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
				RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
				String rgID = regions
						.getApplicableRegionsIDs(
								BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
						.get(0);
				OnInvClick.pasten(auswahl, rgID, p);
				OnInvClick.playerLastPaste.put(p, auswahl);
				return true;
			} else {
				return false;
			}

		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public Inventory inventarErstellen(Player p) {
		String inventoryName = StringGetterBau.getString(p, "testBlockSklaveTierInv");
		Inventory inv = Bukkit.createInventory(null, 9, inventoryName);

//erstes item: letzter Block

		ItemStack is2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);

		SkullMeta meta = (SkullMeta) is2.getItemMeta();
		meta.setDisplayName(StringGetterBau.getString(p, "lastPaste"));
		meta.setOwner(p.getName());
		is2.setItemMeta(meta);
		inv.addItem(is2);

//4. Tier 1

		inv.setItem(3, Banner.ONE.setName("§rTier I"));
//5. Tier 2
		inv.setItem(4, Banner.TWO.setName("§rTier II"));
//6. Tier 3/Tier 4
		inv.setItem(5, Banner.THREE.setName("§rTier III/IV"));
		// inv.setItem(6, Banner.FOUR.setName("§rTier IV"));
		// 9. gui/close
		ItemStack close = new ItemStack(Material.BARRIER);
		ItemMeta closeim = close.getItemMeta();
		closeim.setDisplayName(StringGetterBau.getString(p, "close"));
		close.setItemMeta(closeim);
		inv.setItem(8, close);
//bei den tieren kommt danach die Richtung und danach ob mit Schuld oder ohne. das wir dann in der 2 gespeichert(letzter Block..)
		return inv;
	}
}
