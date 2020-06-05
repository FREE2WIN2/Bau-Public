package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.StringGetterBau;
import de.AS.Bau.utils.ItemStackCreator;

public enum ShieldPosition {

	FRONT, BACK, ROOFFRONT, ROOFBACK, RIGHTSIDEFRONT, RIGHTSIDEBACK, LEFTSIDEFRONT, LEFTSIDEBACK;

	public int getPositionInGUI() {
		switch (this) {
		case BACK:
			return 3;

		case LEFTSIDEBACK:
			return 18;

		case ROOFBACK:
			return 21;

		case RIGHTSIDEBACK:
			return 24;

		case LEFTSIDEFRONT:
			return 27;

		case ROOFFRONT:
			return 30;

		case RIGHTSIDEFRONT:
			return 33;

		case FRONT:
			return 48;

		default:
			break;
		}
		return -1;
	}

	public static ShieldPosition getByPositionInGUI(int position) {
		switch (position) {
		case 3:
			return BACK;
		case 18:
			return LEFTSIDEBACK;
		case 21:
			return ROOFBACK;
		case 24:
			return RIGHTSIDEBACK;
		case 27:
			return LEFTSIDEFRONT;
		case 30:
			return ROOFFRONT;
		case 33:
			return RIGHTSIDEFRONT;
		case 48:
			return FRONT;
		}
		return null;
	}

	public ItemStack getPlaceholderItem(Player p) {
		switch (this) {
		case BACK:
			return ItemStackCreator.createNewItemStack(Material.LIME_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case FRONT:
			return ItemStackCreator.createNewItemStack(Material.YELLOW_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case LEFTSIDEBACK:
			return ItemStackCreator.createNewItemStack(Material.BLUE_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case LEFTSIDEFRONT:
			return ItemStackCreator.createNewItemStack(Material.BLUE_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case RIGHTSIDEBACK:
			return ItemStackCreator.createNewItemStack(Material.RED_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case RIGHTSIDEFRONT:
			return ItemStackCreator.createNewItemStack(Material.RED_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case ROOFBACK:
			return ItemStackCreator.createNewItemStack(Material.BLACK_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		case ROOFFRONT:
			return ItemStackCreator.createNewItemStack(Material.BLACK_STAINED_GLASS_PANE,
					StringGetterBau.getString(p, "tbs_editor_" + this.name()));
		}
		return null;
	}
}
