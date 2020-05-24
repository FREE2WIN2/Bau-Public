package de.AS.Bau.Tools.Particles;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.StringGetterBau;
import de.AS.Bau.utils.ItemStackCreator;

public class ParticlesGUI implements Listener {

	public static void open(Player p) {
		p.openInventory(createParticlesGUI(p));
	}

	private static Inventory createParticlesGUI(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, "§6Particles�7GUI");
		ItemStack toggleOn = ItemStackCreator.createNewItemStack(Material.GLASS_BOTTLE,
				StringGetterBau.getString(p, "particles_gui_toggleOn"));
		ItemStack toggleOff = ItemStackCreator.createNewItemStack(Material.EXPERIENCE_BOTTLE,
				StringGetterBau.getString(p, "particles_gui_toggleOff"));
		ItemStack colorClip = ItemStackCreator.createNewItemStack(Material.GLOBE_BANNER_PATTERN,
				StringGetterBau.getString(p, "particles_gui_changeColorClipboard"));
		ItemStack colorSel = ItemStackCreator.createNewItemStack(Material.WOODEN_AXE,
				StringGetterBau.getString(p, "particles_gui_changeColorSelection"));
		if (Particles.playersParticlesShow.get(p.getUniqueId()).isActive()) {
			inv.setItem(2, toggleOff);
		} else {
			inv.setItem(2, toggleOn);
		}
		inv.setItem(4, colorSel);
		inv.setItem(6, colorClip);
		return inv;
	}

	private static Inventory createColorGUI(String name) {
		Inventory inv = Bukkit.createInventory(null, 18, name);
		inv.addItem(new ItemStack(Material.BLACK_DYE));
		inv.addItem(new ItemStack(Material.BLUE_DYE));
		inv.addItem(new ItemStack(Material.BROWN_DYE));
		inv.addItem(new ItemStack(Material.CYAN_DYE));
		inv.addItem(new ItemStack(Material.GRAY_DYE));
		inv.addItem(new ItemStack(Material.GREEN_DYE));
		inv.addItem(new ItemStack(Material.LIGHT_BLUE_DYE));
		inv.addItem(new ItemStack(Material.LIGHT_GRAY_DYE));
		inv.addItem(new ItemStack(Material.LIME_DYE));
		inv.addItem(new ItemStack(Material.MAGENTA_DYE));
		inv.addItem(new ItemStack(Material.ORANGE_DYE));
		inv.addItem(new ItemStack(Material.PINK_DYE));
		inv.addItem(new ItemStack(Material.PURPLE_DYE));
		inv.addItem(new ItemStack(Material.RED_DYE));
		inv.addItem(new ItemStack(Material.WHITE_DYE));
		inv.addItem(new ItemStack(Material.YELLOW_DYE));
		return inv;
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClickInInventory(InventoryClickEvent event) {
		ItemStack clicked = event.getCurrentItem();
		Player p = (Player) event.getWhoClicked();
		if (clicked == null) {
			return;
		}
		if (event.getView().getTitle().equals("§6Particles�7GUI")) {
			String clickedName = clicked.getItemMeta().getDisplayName();
			if (clickedName.equalsIgnoreCase(StringGetterBau.getString(p, "particles_gui_toggleOn"))) {
				p.performCommand("particles on");
				p.closeInventory();
			} else if (clickedName.equalsIgnoreCase(StringGetterBau.getString(p, "particles_gui_toggleOff"))) {
				p.performCommand("particles off");
				p.closeInventory();
			} else if (clickedName
					.equalsIgnoreCase(StringGetterBau.getString(p, "particles_gui_changeColorClipboard"))) {
				p.openInventory(
						createColorGUI(StringGetterBau.getString(p, "particles_gui_changeColorClipboardTitle")));
			} else if (clickedName
					.equalsIgnoreCase(StringGetterBau.getString(p, "particles_gui_changeColorSelection"))) {
				p.openInventory(
						createColorGUI(StringGetterBau.getString(p, "particles_gui_changeColorSelectionTitle")));
			}
			event.setCancelled(true);
		} else if (event.getView().getTitle().equals(StringGetterBau.getString(p, "particles_gui_changeColorClipboardTitle"))) {
			DyeColor color = DyeColor.getByDyeData(clicked.getData().getData());
			event.setCancelled(true);
			p.closeInventory();
			p.performCommand("particles clipboard " + color.name());
		
		} else if (event.getView().getTitle().equals(StringGetterBau.getString(p, "particles_gui_changeColorSelectionTitle"))) {
			DyeColor color = DyeColor.getByDyeData(clicked.getData().getData());
			event.setCancelled(true);
			p.closeInventory();
			p.performCommand("particles selection " + color.name());
		}

	}
}
