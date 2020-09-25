package net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockEditor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;

public class TestBlockEditorCore implements Listener {

	public static Map<UUID, TestBlockEditor> playersTestBlockEditor;

	public TestBlockEditorCore() {
		playersTestBlockEditor = new HashMap<>();
	}

	@EventHandler
	public void invClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		if (event.getClickedInventory() == null) {
			return;
		}
		Inventory pInv = p.getOpenInventory().getTopInventory();
		String invName = p.getOpenInventory().getTitle();
		ItemStack clicked = event.getCurrentItem();
		if (clicked == null) {
			return;
		}
		if (!(clicked.hasItemMeta())) {
			return;
		}
		if (pInv.getType().equals(InventoryType.CREATIVE)) {
			return;
		}
		if (invName.equals("§7TestBlock-§6Editor")) {
			event.setCancelled(true);
			editorInv(p, clicked, event.getSlot());
			return;
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_editor_chooseTypeInv"))) {
			event.setCancelled(true);
			chooseTypeInv(p, clicked);
			return;
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_editor_tierInv"))) {
			event.setCancelled(true);
			tierInv(p, clicked);
			return;
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_editor_facingInv"))) {
			event.setCancelled(true);
			facingInv(p, clicked);
			return;
		}
	}

	private void facingInv(Player p, ItemStack clicked) {
		String clickedName = clicked.getItemMeta().getDisplayName();
		String south = MessageHandler.getInstance().getString(p, "facingSouth");
		String north = MessageHandler.getInstance().getString(p, "facingNorth");
		if (north.equals(clickedName)) {
			getEditor(p).setFacing(Facing.NORTH);
		} else if (south.equals(clickedName)) {
			getEditor(p).setFacing(Facing.SOUTH);
		}
		getEditor(p).openTierInv();
	}

	private void tierInv(Player p, ItemStack clicked) {
		String clickedName = clicked.getItemMeta().getDisplayName();
		int tier = 0;
		switch (clickedName) {
		case "§rTier I":
			tier = 1;
			break;
		case "§rTier II":
			tier = 2;
			break;
		case "§rTier III/IV":
			tier = 3;
			break;
		}
		getEditor(p).setTier(tier);
		getEditor(p).visualize();
		p.closeInventory();

	}

	private void chooseTypeInv(Player p, ItemStack clicked) {
		ShieldType type = ShieldType.getByItem(clicked);

		getEditor(p).removeModule(getEditor(p).getPos());
		if (type != null) {
			getEditor(p).addModule(type);
		}
		getEditor(p).openMainInv();

	}

	private void editorInv(Player p, ItemStack clicked, int slot) {
		String clickedName = clicked.getItemMeta().getDisplayName();
		ShieldPosition pos = ShieldPosition.getByPositionInGUI(slot);
		if (pos != null) {
			getEditor(p).setChoosedPosition(pos);
			getEditor(p).openChooseTypeInv();
			return;
		}
		if (clickedName.equals(MessageHandler.getInstance().getString(p, "tbs_editor_mainInv_save"))) {
			getEditor(p).startSave();
		} else if (clickedName.equals(MessageHandler.getInstance().getString(p, "tbs_editor_mainInv_reset"))) {
			getEditor(p).reset();
		} else if (clickedName.equals(MessageHandler.getInstance().getString(p, "tbs_editor_mainInv_show"))) {
			getEditor(p).startVisualize();
		}

	}

	public static TestBlockEditor getEditor(Player p) {
		if (!playersTestBlockEditor.containsKey(p.getUniqueId())) {
			playersTestBlockEditor.put(p.getUniqueId(), new TestBlockEditor(p));
		}
		return playersTestBlockEditor.get(p.getUniqueId());
	}

}
