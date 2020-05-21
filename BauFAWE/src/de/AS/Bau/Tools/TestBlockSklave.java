package de.AS.Bau.Tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.WorldEdit.UndoManager;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.Banner;

public class TestBlockSklave implements CommandExecutor, Listener {

	public static HashMap<UUID, String> playerLastPaste = new HashMap<>();
	public static HashSet<Player> playerBlockedDelete = new HashSet<>();
	public HashMap<UUID, String> playersCurrentSelection = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmds, String string, String[] args) {
		Player p = (Player) sender;
		if (args.length == 0) {
			p.openInventory(tbsStartInv(p));
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("last")) {

				if (playerLastPaste.containsKey(p.getUniqueId())) {
					RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
					RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
					String rgID = regions.getApplicableRegionsIDs(
							BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
							.get(0);

					/* paste */

					WorldEditHandler.pasten(playerLastPaste.get(p.getUniqueId()), rgID, p, true, true,true);
					return true;
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noLastPaste"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("undo")) {
				/* Undo last TB */
				UndoManager manager;
				if (!Main.playersUndoManager.containsKey(p.getUniqueId())) {
					Main.send(p, "tbs_noUndo");
					return true;
				} else {
					manager = Main.playersUndoManager.get(p.getUniqueId());
				}
				Clipboard undo = manager.getUndo();
				if (undo == null) {
					Main.send(p, "tbs_noUndo");
					return true;
				}
				int x = undo.getOrigin().getBlockX();
				int y = undo.getOrigin().getBlockY();
				int z = undo.getOrigin().getBlockZ();
				WorldEditHandler.pasteAsync(new ClipboardHolder(undo), BlockVector3.at(x, y, z), p, false, 1, false,true);
				Main.send(p, "tbs_undo");
				return true;
			} else {
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
				String rgID = WorldGuardHandler.getPlotId(p.getLocation());

				/* paste */

				WorldEditHandler.pasten(auswahl, rgID, p, true, true,true);
				playerLastPaste.put(p.getUniqueId(), auswahl);
				return true;
			} else {
				return false;
			}

		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public Inventory tbsStartInv(Player p) {
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

	@EventHandler
	public void onInventoryclick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Inventory pInv = p.getOpenInventory().getTopInventory();
		String invName = p.getOpenInventory().getTitle();
		ItemStack clicked = event.getCurrentItem();
		if (clicked != null) {
			if (clicked.getType().equals(Material.NETHER_STAR) || clicked.getType().equals(Material.WOODEN_HOE)) {
				return;
			}
			if (clicked.hasItemMeta()) {
				if (!pInv.getType().equals(InventoryType.CREATIVE)) {
					if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveTierInv"))
							&& event.getClickedInventory().equals(pInv)) {

						event.setCancelled(true);
						tierINventory(p, pInv, clicked);
					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveFacingInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						facingInv(p, pInv, clicked);

					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveTypeInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						typeInv(p, pInv, clicked);

					}

				}
			}
		}

	}

	private void tierINventory(Player p, Inventory pInv, ItemStack clicked) {
		String close = StringGetterBau.getString(p, "close");
		String lastPaste = StringGetterBau.getString(p, "lastPaste");
		String clickedName = clicked.getItemMeta().getDisplayName();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		String rgID = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())).get(0);
		switch (clickedName) {
		case "§rTier I":
			playersCurrentSelection.put(p.getUniqueId(), "T1_");
			p.openInventory(richtungsInventory(p));
			break;
		case "§rTier II":
			playersCurrentSelection.put(p.getUniqueId(), "T2_");
			p.openInventory(richtungsInventory(p));
			break;
		case "§rTier III/IV":
		case "§rTier IV":
			playersCurrentSelection.put(p.getUniqueId(), "T3_");
			p.openInventory(richtungsInventory(p));
			break;
		}
		if (clickedName.equals(close)) {
			p.closeInventory();
		} else if (clickedName.equals(lastPaste)) {
			if (playerLastPaste.containsKey(p.getUniqueId())) {

				WorldEditHandler.pasten(playerLastPaste.get(p.getUniqueId()), rgID, p, true, true,true);
				p.closeInventory();
			} else {
				p.closeInventory();
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noLastPaste"));
			}
		}

	}

	private void facingInv(Player p, Inventory pInv, ItemStack clicked) {
		String south = StringGetterBau.getString(p, "facingSouth");
		String north = StringGetterBau.getString(p, "facingNorth");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String current = playersCurrentSelection.get(p.getUniqueId());
		if (north.equals(clickedName)) {
			current += "N_";
		} else if (south.equals(clickedName)) {
			current += "S_";
		}
		playersCurrentSelection.put(p.getUniqueId(), current);
		p.openInventory(schildRahmenNormalInventory(p));
	}

	private void typeInv(Player p, Inventory pInv, ItemStack clicked) {
		String normal = "§rNormal";
		String frame = StringGetterBau.getString(p, "frame");
		String shield = StringGetterBau.getString(p, "shield");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String current = playersCurrentSelection.get(p.getUniqueId());
		if (clickedName.equals(normal)) {
			current += "N";
		} else if (clickedName.equals(frame)) {
			current += "F";
		} else if (clickedName.equals(shield)) {
			current += "S";
		}
		p.closeInventory();
		String rgID = WorldGuardHandler.getPlotId(p.getLocation());
		WorldEditHandler.pasten(current, rgID, p, true, true,true);
		playerLastPaste.put(p.getUniqueId(), current);
	}

	public Inventory richtungsInventory(Player p) {
		// norden

		ItemStack isN = Banner.N.setName(StringGetterBau.getString(p, "facingNorth"));
		// süden
		ItemStack isS = Banner.S.setName(StringGetterBau.getString(p, "facingSouth"));
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveFacingInv"));
		inv.setItem(2, isN);
		inv.setItem(6, isS);
		return inv;
	}

	public Inventory schildRahmenNormalInventory(Player p) {
		// Schild
		ItemStack isSchild = new ItemStack(Material.SHIELD);
		ItemMeta imSchild = isSchild.getItemMeta();
		imSchild.setDisplayName(StringGetterBau.getString(p, "shield"));
		isSchild.setItemMeta(imSchild);
		// Rahmen
		ItemStack isRahmen = new ItemStack(Material.SCAFFOLDING);
		ItemMeta imRahmen = isRahmen.getItemMeta();
		imRahmen.setDisplayName(StringGetterBau.getString(p, "frame"));
		isRahmen.setItemMeta(imRahmen);
		// normal
		ItemStack isNormal = new ItemStack(Material.WHITE_WOOL);
		ItemMeta imNormal = isNormal.getItemMeta();
		imNormal.setDisplayName("§rNormal");
		isNormal.setItemMeta(imNormal);
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveTypeInv"));
		inv.setItem(1, isSchild);
		inv.setItem(4, isNormal);
		inv.setItem(7, isRahmen);
		return inv;
	}

}
