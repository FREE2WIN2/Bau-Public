package net.wargearworld.bau.tools.testBlockSlave.testBlockEditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.utils.ItemStackCreator;

public enum ShieldPosition {

	FRONT, BACK, ROOFFRONT, ROOFBACK, RIGHTSIDEFRONT, RIGHTSIDEBACK, LEFTSIDEFRONT, LEFTSIDEBACK;

	public int getPositionInGUI() {
		switch (this) {
		case FRONT:
			return 3;

		case LEFTSIDEFRONT:
			return 18;

		case ROOFFRONT:
			return 21;

		case RIGHTSIDEFRONT:
			return 24;

		case LEFTSIDEBACK:
			return 27;

		case ROOFBACK:
			return 30;

		case RIGHTSIDEBACK:
			return 33;

		case BACK:
			return 48;

		default:
			break;
		}
		return -1;
	}

	public static ShieldPosition getByPositionInGUI(int position) {
		switch (position) {
		case 3:
			return FRONT;
		case 18:
			return LEFTSIDEFRONT;
		case 21:
			return ROOFFRONT;
		case 24:
			return RIGHTSIDEFRONT;
		case 27:
			return LEFTSIDEBACK;
		case 30:
			return ROOFBACK;
		case 33:
			return RIGHTSIDEBACK;
		case 48:
			return BACK;
		}
		return null;
	}

	public ItemStack getPlaceholderItem(Player p) {
		switch (this) {
		case BACK:
			return ItemStackCreator.createNewItemStack(Material.LIME_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case FRONT:
			return ItemStackCreator.createNewItemStack(Material.YELLOW_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case LEFTSIDEBACK:
			return ItemStackCreator.createNewItemStack(Material.BLUE_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case LEFTSIDEFRONT:
			return ItemStackCreator.createNewItemStack(Material.BLUE_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case RIGHTSIDEBACK:
			return ItemStackCreator.createNewItemStack(Material.RED_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case RIGHTSIDEFRONT:
			return ItemStackCreator.createNewItemStack(Material.RED_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case ROOFBACK:
			return ItemStackCreator.createNewItemStack(Material.BLACK_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		case ROOFFRONT:
			return ItemStackCreator.createNewItemStack(Material.BLACK_STAINED_GLASS_PANE,
					MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
		}
		return null;
	}
}