package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum ShieldType {

	SAND, MASSIVE, ARTOX, SPIKE, ARTILLERY;

	public ItemStack getItem(Player p) {
		switch (this) {
		case ARTILLERY:
		case ARTOX:
		case MASSIVE:
		case SAND:
		case SPIKE:
		}
		return null;
	}
}
