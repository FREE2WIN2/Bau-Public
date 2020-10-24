package net.wargearworld.bau.tools.testBlockSlave.testBlockEditor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.bau.utils.ItemStackCreator;

public enum ShieldType {

	SAND, MASSIVE, ARTOX, SPIKE, ARTILLERY,BACKSTAB;

	public ItemStack getItem() {
		switch (this) {
		case ARTILLERY:
			return ItemStackCreator.createNewItemStack(Material.OAK_FENCE_GATE, "§6Arti");
		case ARTOX:
			return ItemStackCreator.createNewItemStack(Material.SLIME_BLOCK, "§6Artox");
		case MASSIVE:
			return ItemStackCreator.createNewItemStack(Material.END_STONE, "§6Massiv");
		case SAND:
			return ItemStackCreator.createNewItemStack(Material.SAND, "§6Sand");
		case SPIKE:
			return ItemStackCreator.createNewItemStack(Material.END_ROD, "§6Spike");
		case BACKSTAB:
			return ItemStackCreator.createNewItemStack(Material.WHITE_BED, "§6Backstab");
		default:
			break;
		}
		return null;
	}
	
	public static ShieldType getByItem(ItemStack item) {
		for(ShieldType type:values()) {
			if(item.equals(type.getItem())) {
				return type;
			}
		}
		return null;
	}
}
