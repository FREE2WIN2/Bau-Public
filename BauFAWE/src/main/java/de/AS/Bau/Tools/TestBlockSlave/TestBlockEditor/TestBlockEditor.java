package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import de.AS.Bau.StringGetterBau;

public class TestBlockEditor {

	/*
	 * This class manage everything about the TestBlock-Editor ingame 
	 * 
	 * 
	 */
	
	private HashSet<ShieldModule> modules;
	public TestBlockEditor() {
		modules = new HashSet<>();
	}
	
	public Inventory createInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, 54,StringGetterBau.getString(p, "§7TestBlock-§6Editor"));
		
		return inv;
	}
	
	public void addModule(ShieldModule module) {
		modules.add(module);
	}
	
	
}
