package net.wargearworld.bau.tools.testBlock.testBlockEditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.utils.ItemStackCreator;

public enum ShieldPosition {

    FRONT, FRONTRIGHT, FRONTLEFT, BACK, ROOFFRONT, ROOFBACK, RIGHTSIDEFRONT, RIGHTSIDEBACK, LEFTSIDEFRONT, LEFTSIDEBACK;

    public int getPositionInGUI() {
        switch (this) {
            case FRONTLEFT:
                return 51;
            case FRONT:
                return 48;
            case FRONTRIGHT:
                return 45;

            case LEFTSIDEFRONT:
                return 33;

            case ROOFFRONT:
                return 30;

            case RIGHTSIDEFRONT:
                return 27;

            case LEFTSIDEBACK:
                return 24;

            case ROOFBACK:
                return 21;

            case RIGHTSIDEBACK:
                return 18;

            case BACK:
                return 3;

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
                return RIGHTSIDEBACK;
            case 21:
                return ROOFBACK;
            case 24:
                return LEFTSIDEBACK;
            case 27:
                return RIGHTSIDEFRONT;
            case 30:
                return ROOFFRONT;
            case 33:
                return LEFTSIDEFRONT;
            case 45:
                return FRONTRIGHT;
            case 48:
                return FRONT;
            case 51:
                return FRONTLEFT;
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

            case FRONTLEFT:
            case LEFTSIDEBACK:
            case LEFTSIDEFRONT:
                return ItemStackCreator.createNewItemStack(Material.BLUE_STAINED_GLASS_PANE,
                        MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
            case FRONTRIGHT:
            case RIGHTSIDEBACK:
            case RIGHTSIDEFRONT:
                return ItemStackCreator.createNewItemStack(Material.RED_STAINED_GLASS_PANE,
                        MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
            case ROOFBACK:
            case ROOFFRONT:
                return ItemStackCreator.createNewItemStack(Material.BLACK_STAINED_GLASS_PANE,
                        MessageHandler.getInstance().getString(p, "tbs_editor_" + this.name()));
        }
        return null;
    }

}