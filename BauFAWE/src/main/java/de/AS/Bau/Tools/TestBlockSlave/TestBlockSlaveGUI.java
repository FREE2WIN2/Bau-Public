package de.AS.Bau.Tools.TestBlockSlave;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.CustomTestBlock;
import de.AS.Bau.utils.Banner;
import de.AS.Bau.utils.ItemStackCreator;

public class TestBlockSlaveGUI implements Listener {

	/*
	 * Main gui: save Block as own TestBlock personal tbs (nether star?) TBS Editor
	 *
	 * Paste default TB(old TBS)
	 *
	 * favorites manage favorites
	 * 
	 * favorties : click and choose direction : If I have to rotate them: z--
	 * banner: tier 1 with yellow background add: per personal tb-> add To favorites
	 * max favs: 9
	 * 
	 * save own Block: wodenAxe as symbol: -> stand on the right plot! -> choose
	 * facing of the given on -> Name into anvil Inventory(Name banner?) -> then
	 * saving
	 */

	public static Inventory tbsStartInv(Player p, Set<CustomTestBlock> favorites) {
		String inventoryName = StringGetterBau.getString(p, "testBlockSklaveMainInv");
		Inventory inv = Bukkit.createInventory(null, 45, inventoryName);

		inv.setItem(2, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, "§6Editor"));
		inv.setItem(4, ItemStackCreator.createNewItemStack(Material.WOODEN_AXE,
				StringGetterBau.getString(p, "tbs_gui_newTB")));
		inv.setItem(6, ItemStackCreator.createNewItemStack(Material.NETHER_STAR,
				StringGetterBau.getString(p, "tbs_gui_tbManager")));

		ItemStack head = ItemStackCreator.createNewItemStack(Material.PLAYER_HEAD,
				StringGetterBau.getString(p, "tbs_gui_lastPaste"));
		SkullMeta headmeta = (SkullMeta) head.getItemMeta();
		headmeta.setOwningPlayer(p);
		head.setItemMeta(headmeta);
		inv.setItem(18, head);

		inv.setItem(21, Banner.ONE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier I"));
		inv.setItem(22, Banner.TWO.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier II"));
		inv.setItem(23, Banner.THREE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier III/IV"));

		inv.setItem(26,
				ItemStackCreator.createNewItemStack(Material.BARRIER, StringGetterBau.getString(p, "tbs_gui_close")));

		int favIndex = 36;
		for (CustomTestBlock tb : favorites) {
			if (favIndex >= 45) {
				System.err.println("§c[TBS] §6TOO MANY FAVORITES: " + p.getName());
				break;
			}
			inv.setItem(favIndex, tb.getBanner());
			favIndex++;
		}
		for (int i = favIndex; i < 45; i++) {
			inv.setItem(i, Banner.PLUS.create(DyeColor.WHITE, DyeColor.BLACK,
					StringGetterBau.getString(p, "tbs_gui_addFavorites")));
		}
		return inv;
	}

	public static Inventory richtungsInventory(Player p) {
		// norden

		ItemStack isN = Banner.N.create(DyeColor.WHITE, DyeColor.BLACK, StringGetterBau.getString(p, "facingNorth"));
		// süden
		ItemStack isS = Banner.S.create(DyeColor.WHITE, DyeColor.BLACK, StringGetterBau.getString(p, "facingSouth"));
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveFacingInv"));
		inv.setItem(2, isN);
		inv.setItem(6, isS);
		return inv;
	}

	public static Inventory schildRahmenNormalInventory(Player p) {
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveTypeInv"));
		inv.setItem(1, ItemStackCreator.createNewItemStack(Material.SHIELD, StringGetterBau.getString(p, "shield")));
		inv.setItem(4, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, "§rNormal"));
		inv.setItem(7,
				ItemStackCreator.createNewItemStack(Material.SCAFFOLDING, StringGetterBau.getString(p, "frame")));
		return inv;
	}

	public static Inventory tbManager(HashMap<Integer, HashSet<CustomTestBlock>> tbs, Player p) {
		Inventory inv = Bukkit.createInventory(null, 54,
				StringGetterBau.getString(p, "tbs_tbManagerInv").replace("%r", p.getName()));
		inv.setItem(1, ItemStackCreator.createNewItemStack(Material.BARRIER,
				StringGetterBau.getString(p, "tbs_gui_tbManagerDelete")));
		inv.setItem(3, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL,
				StringGetterBau.getString(p, "tbs_gui_tbManagerPaste")));
		inv.setItem(5, Banner.PLUS.create(DyeColor.WHITE, DyeColor.BLACK,
				StringGetterBau.getString(p, "tbs_gui_tbManagerFavorite")));
		inv.setItem(7, Banner.MINUS.create(DyeColor.WHITE, DyeColor.BLACK,
				StringGetterBau.getString(p, "tbs_gui_tbManagerFavoriteRemove")));

		ItemStack whiteBanner = ItemStackCreator.createNewItemStack(Material.WHITE_BANNER, " ");

		for (Entry<Integer, HashSet<CustomTestBlock>> entry : tbs.entrySet()) {
			int index = (entry.getKey() * 18) - 9;

			for (CustomTestBlock block : entry.getValue()) {
				inv.setItem(index, block.getBanner());
				index++;
			}
			for (int i = index; i < (entry.getKey() * 18); i++) {
				inv.setItem(i, whiteBanner);
			}
		}
		return inv;
	}

	public static Inventory showAllNonFavorites(HashMap<Integer, HashSet<CustomTestBlock>> tbs, Player p) {
		Inventory inv = Bukkit.createInventory(null, 27, StringGetterBau.getString(p, "tbs_gui_addFavoriteInv"));
		ItemStack whiteBanner = ItemStackCreator.createNewItemStack(Material.WHITE_BANNER, " ");
		for (Entry<Integer, HashSet<CustomTestBlock>> entry : tbs.entrySet()) {
			int index = (entry.getKey() * 9) - 9;

			for (CustomTestBlock block : entry.getValue()) {
				if (!block.isFavorite()) {
					inv.setItem(index, block.getBanner());
					index++;
				}
			}
			for (int i = index; i < (entry.getKey() * 9); i++) {
				inv.setItem(i, whiteBanner);
			}
		}
		return inv;
	}

	
	public static Inventory schildNormalInventory(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveTypeInv"));
		inv.setItem(2, ItemStackCreator.createNewItemStack(Material.SHIELD, StringGetterBau.getString(p, "shield")));
		inv.setItem(6, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, "§rNormal"));
		return inv;
	}

	public static Inventory tierInv(Player p) {
		String inventoryName = StringGetterBau.getString(p, "tbs_gui_tierInv");
		Inventory inv = Bukkit.createInventory(null, 9, inventoryName);
		inv.setItem(2, Banner.ONE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier I"));
		inv.setItem(4, Banner.TWO.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier II"));
		inv.setItem(6, Banner.THREE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier III/IV"));
		return inv;
	}
	
	public static Inventory ChooseNameInv(Player p,int tier) {
		Inventory inv = (AnvilInventory) Bukkit.createInventory(null, InventoryType.ANVIL);
		if(tier == 1) {
			inv.setItem(0, Banner.ONE.create(DyeColor.ORANGE, DyeColor.BLACK, "§rUnnamed"));
		}else if(tier ==2) {
			inv.setItem(0, Banner.TWO.create(DyeColor.ORANGE, DyeColor.BLACK, "§rUnnamed"));
		}else if(tier == 3) {
			inv.setItem(0, Banner.THREE.create(DyeColor.ORANGE, DyeColor.BLACK, "§rUnnamed"));
		}
		
		return inv;
	}
}
