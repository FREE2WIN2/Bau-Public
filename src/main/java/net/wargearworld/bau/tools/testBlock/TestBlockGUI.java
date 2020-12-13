package net.wargearworld.bau.tools.testBlock;

import net.wargearworld.GUI_API.GUI.AnvilGUI;
import net.wargearworld.GUI_API.GUI.Slot;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.testBlock.testBlock.CustomTestBlock;
import net.wargearworld.bau.utils.Banner;
import net.wargearworld.bau.utils.ItemStackCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


public class TestBlockGUI implements Listener {

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
	 * save own Block: wodenAxe as symbol: stand on the right plot! choose
	 * facing of the given on Name into anvil Inventory(Name banner?) then
	 * saving
	 */

	public static Inventory tbsStartInv(Player p, Set<CustomTestBlock> favorites) {
		String inventoryName = MessageHandler.getInstance().getString(p, "testBlockSklaveMainInv");
		Inventory inv = Bukkit.createInventory(null, 45, inventoryName);

		inv.setItem(2, ItemStackCreator.createNewItemStack(Material.CRAFTING_TABLE, "§6Editor"));
		inv.setItem(4, ItemStackCreator.createNewItemStack(Material.WOODEN_AXE,
				MessageHandler.getInstance().getString(p, "tbs_gui_newTB")));
		inv.setItem(6, ItemStackCreator.createNewItemStack(Material.NETHER_STAR,
				MessageHandler.getInstance().getString(p, "tbs_gui_tbManager")));

		ItemStack head = ItemStackCreator.createNewItemStack(Material.PLAYER_HEAD,
				MessageHandler.getInstance().getString(p, "tbs_gui_lastPaste"));
		SkullMeta headmeta = (SkullMeta) head.getItemMeta();
		headmeta.setOwningPlayer(p);
		head.setItemMeta(headmeta);
		inv.setItem(18, head);

		inv.setItem(21, Banner.ONE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier I"));
		inv.setItem(22, Banner.TWO.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier II"));
		inv.setItem(23, Banner.THREE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier III/IV"));

		inv.setItem(26,
				ItemStackCreator.createNewItemStack(Material.BARRIER, MessageHandler.getInstance().getString(p, "tbs_gui_close")));

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
					MessageHandler.getInstance().getString(p, "tbs_gui_addFavorites")));
		}
		return inv;
	}

	public static Inventory richtungsInventory(Player p) {
		// norden

		ItemStack isN = Banner.N.create(DyeColor.WHITE, DyeColor.BLACK, MessageHandler.getInstance().getString(p, "facingNorth"));
		// süden
		ItemStack isS = Banner.S.create(DyeColor.WHITE, DyeColor.BLACK, MessageHandler.getInstance().getString(p, "facingSouth"));
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, MessageHandler.getInstance().getString(p, "testBlockSklaveFacingInv"));
		inv.setItem(2, isN);
		inv.setItem(6, isS);
		return inv;
	}

	public static Inventory schildRahmenNormalInventory(Player p) {
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, MessageHandler.getInstance().getString(p, "testBlockSklaveTypeInv"));
		inv.setItem(1, ItemStackCreator.createNewItemStack(Material.SHIELD, MessageHandler.getInstance().getString(p, "shield")));
		inv.setItem(4, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, "§rNormal"));
		inv.setItem(7,
				ItemStackCreator.createNewItemStack(Material.SCAFFOLDING, MessageHandler.getInstance().getString(p, "frame")));
		return inv;
	}

	public static Inventory tbManager(HashMap<Integer, Set<CustomTestBlock>> tbs, Player p) {

		Inventory inv = Bukkit.createInventory(null, 45,
				MessageHandler.getInstance().getString(p, "tbs_tbManagerInv").replace("%r", p.getName()));
		ItemStack whiteBanner = ItemStackCreator.createNewItemStack(Material.WHITE_BANNER, " ");

		for (Entry<Integer, Set<CustomTestBlock>> entry : tbs.entrySet()) {
			int index = (entry.getKey()-1) * 18;

			for (CustomTestBlock block : entry.getValue()) {
				inv.setItem(index, block.getBanner());
				index++;
			}
			for (int i = index; i < (entry.getKey() * 18 -9); i++) {
				if(i>=45) {
					break;
				}
				inv.setItem(i, whiteBanner);
			}
		}
		return inv;
	}
	
	public static Inventory tbManagerActionInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9,
				MessageHandler.getInstance().getString(p, "tbs_tbManagerActionInv").replace("%r", p.getName()));
		inv.setItem(1, ItemStackCreator.createNewItemStack(Material.BARRIER,
				MessageHandler.getInstance().getString(p, "tbs_gui_tbManagerDelete")));
		inv.setItem(3, Banner.N.create(DyeColor.WHITE, DyeColor.BLACK, MessageHandler.getInstance().getString(p, "facingNorth")));
		inv.setItem(4, Banner.S.create(DyeColor.WHITE, DyeColor.BLACK, MessageHandler.getInstance().getString(p, "facingSouth")));
		inv.setItem(6, Banner.PLUS.create(DyeColor.WHITE, DyeColor.BLACK,
				MessageHandler.getInstance().getString(p, "tbs_gui_tbManagerFavorite")));
		inv.setItem(8, Banner.MINUS.create(DyeColor.WHITE, DyeColor.BLACK,
				MessageHandler.getInstance().getString(p, "tbs_gui_tbManagerFavoriteRemove")));
		return inv;
	}

	public static Inventory showAllNonFavorites(HashMap<Integer, Set<CustomTestBlock>> tbs, Player p) {
		Inventory inv = Bukkit.createInventory(null, 27, MessageHandler.getInstance().getString(p, "tbs_gui_addFavoriteInv"));
		ItemStack whiteBanner = ItemStackCreator.createNewItemStack(Material.WHITE_BANNER, " ");
		for (Entry<Integer, Set<CustomTestBlock>> entry : tbs.entrySet()) {
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
		Inventory inv = Bukkit.createInventory(null, 9, MessageHandler.getInstance().getString(p, "testBlockSklaveTypeInv"));
		inv.setItem(2, ItemStackCreator.createNewItemStack(Material.SHIELD, MessageHandler.getInstance().getString(p, "shield")));
		inv.setItem(6, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, "§rNormal"));
		return inv;
	}

	public static Inventory tierInv(Player p) {
		String inventoryName = MessageHandler.getInstance().getString(p, "tbs_gui_tierInv");
		Inventory inv = Bukkit.createInventory(null, 9, inventoryName);
		inv.setItem(2, Banner.ONE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier I"));
		inv.setItem(4, Banner.TWO.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier II"));
		inv.setItem(6, Banner.THREE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier III/IV"));
		return inv;
	}
	
	public static void ChooseNameInv(Player p,int tier) {

		AnvilGUI gui = new AnvilGUI(MessageHandler.getInstance().getString(p, "tbs_gui_anvilTitle"), " ");
		if(tier == 1) {
			gui.setItem(Slot.INPUT_LEFT,new DefaultItem(Banner.ONE.create(DyeColor.ORANGE, DyeColor.BLACK, " "),s->{}));
		}else if(tier ==2) {
			gui.setItem(Slot.INPUT_LEFT,new DefaultItem(Banner.TWO.create(DyeColor.ORANGE, DyeColor.BLACK, " "),s->{}));
		}else if(tier == 3) {
			gui.setItem(Slot.INPUT_LEFT,new DefaultItem(Banner.THREE.create(DyeColor.ORANGE, DyeColor.BLACK, " "),s->{}));
		}
		gui.open(p);
	}
}
