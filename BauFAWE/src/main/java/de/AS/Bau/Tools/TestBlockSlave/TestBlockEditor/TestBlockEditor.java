package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.StringGetterBau;
import de.AS.Bau.utils.ItemStackCreator;

public class TestBlockEditor {

	/*
	 * This class manage everything about the TestBlock-Editor ingame
	 * 
	 * 
	 */

	private Set<ShieldModule> modules;
	private Player owner;

	public TestBlockEditor(Player owner) {
		modules = new HashSet<>();
		this.owner = owner;
	}

	public void openMainInv() {
		Inventory inv = Bukkit.createInventory(null, 54, "§7TestBlock-§6Editor");
		int[] woolPos = { 10, 11, 12, 13, 14, 19, 23, 28, 32, 37, 41, 46, 47, 48, 49, 50 };
		/* Wool */
		ItemStack wool = ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, " ");
		for (int i : woolPos) {
			inv.setItem(i, wool);
		}
		/* Components */
		for (ShieldPosition pos : ShieldPosition.values()) {
			inv.setItem(pos.getPositionInGUI(), pos.getPlaceholderItem(owner));
		}
		/* Modules */
		for (ShieldModule module : modules) {
			inv.setItem(module.getPosition().getPositionInGUI(), module.getType().getItem());
		}
		/* Other Actions */
		inv.setItem(17, ItemStackCreator.createNewItemStack(Material.WOODEN_AXE,
				StringGetterBau.getString(owner, "tbs_editor_mainInv_save")));
		inv.setItem(35, ItemStackCreator.createNewItemStack(Material.WOODEN_AXE, StringGetterBau.getString(owner, "tbs_editor_mainInv_show")));
		inv.setItem(54, ItemStackCreator.createNewItemStack(Material.BARRIER, StringGetterBau.getString(owner, "tbs_editor_mainInv_reset")));
		
		owner.openInventory(inv);
	}

	public void openChooseTypeInv() {
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(owner, "tbs_editor_chooseTypeInv"));
		inv.setItem(1, ShieldType.MASSIVE.getItem());
		inv.setItem(2, ShieldType.SAND.getItem());
		inv.setItem(3, ShieldType.SPIKE.getItem());
		inv.setItem(5, ShieldType.ARTOX.getItem());
		inv.setItem(6, ShieldType.ARTILLERY.getItem());
		inv.setItem(7, ShieldType.BACKSTAB.getItem());
		owner.openInventory(inv);
	}

	public void addModule(ShieldModule module) {
		modules.add(module);
	}

}
