package de.AS.Bau.Tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.StringGetterBau;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.ItemStackCreator;

public class PlotTeleporter implements Listener {

	public static void openInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, 27,StringGetterBau.getString(p, "gui_teleporter"));
		// 2pearls
		ItemStack enderPearl1IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotOne"));
		
		ItemStack enderPearl2IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotTwo"));
		enderPearl2IS.setAmount(2);

// 3pearls
		ItemStack enderPearl3IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotThree"));
		enderPearl3IS.setAmount(3);
		
		ItemStack enderPearl4IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotFour"));
		enderPearl4IS.setAmount(4);

// enderEye
		ItemStack enderEye11IS = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
				StringGetterBau.getString(p, "teleportTestPlotTier1One"));
		ItemStack enderEye12IS = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
				StringGetterBau.getString(p, "teleportTestPlotTier1Two"));

// 2eyes
		ItemStack enderEye21IS = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
				StringGetterBau.getString(p, "teleportTestPlotTier2One"));
		enderEye21IS.setAmount(2);
		ItemStack enderEye22IS = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
				StringGetterBau.getString(p, "teleportTestPlotTier2Two"));
		enderEye22IS.setAmount(2);

// 3eyes
		ItemStack enderEye31IS = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
				StringGetterBau.getString(p, "teleportTestPlotTier3One"));
		enderEye31IS.setAmount(3);
		
		ItemStack enderEye32IS = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
				StringGetterBau.getString(p, "teleportTestPlotTier3Two"));
		enderEye32IS.setAmount(3);
		
		inv.setItem(2, enderPearl1IS);
		inv.setItem(3, enderPearl2IS);
		inv.setItem(5, enderPearl3IS);
		inv.setItem(6, enderPearl4IS);
		
		inv.setItem(19, enderEye11IS);
		inv.setItem(20, enderEye12IS);
		inv.setItem(21, enderEye21IS);
		inv.setItem(23, enderEye22IS);
		inv.setItem(24, enderEye31IS);
		inv.setItem(25, enderEye32IS);
		p.openInventory(inv);
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if(event.getClickedInventory() == null) {return;}
		if(event.getSlotType() == SlotType.OUTSIDE) {return;}
		ItemStack item = event.getCurrentItem();
		if(item == null) {return;}
		if(!item.hasItemMeta()) {return;}
			
		
		Player p = (Player) event.getWhoClicked();
		if(!event.getView().getTitle().equals(StringGetterBau.getString(p, "gui_teleporter"))) {return;}
		
		p.teleport(getLocation(p,p.getWorld(),item));
		
	}

	private Location getLocation(Player p,World w,ItemStack item) {
		return CoordGetter.getTeleportLocation(w, getPlotId(p,item));
	}

	private String getPlotId(Player p,ItemStack item) {
		if(item.getType() == Material.ENDER_PEARL) {
			switch(item.getAmount()) {
			case 1:
				return "plot1";
			case 2:
				return "plot2";
			case 3:
				return "plot3";
			case 4:
				return "plot4";
			}
			return "plot2";
		}else if(item.getType() == Material.ENDER_EYE) {
			String itemName = item.getItemMeta().getDisplayName();
			if(itemName.equals(StringGetterBau.getString(p, "teleportTestPlotTier1One"))) {
				return "plottier1test1";
			}else if(itemName.equals(StringGetterBau.getString(p, "teleportTestPlotTier1Two"))) {
				return "plottier1test2";
			}else if(itemName.equals(StringGetterBau.getString(p, "teleportTestPlotTier2One"))) {
				return "plottier2test1";
			}else if(itemName.equals(StringGetterBau.getString(p, "teleportTestPlotTier2Two"))) {
				return "plottier2test2";
			}else if(itemName.equals(StringGetterBau.getString(p, "teleportTestPlotTier3One"))) {
				return "plottier3test1";
			}else if(itemName.equals(StringGetterBau.getString(p, "teleportTestPlotTier3Two"))) {
				return "plottier3test2";
			}
				
		}
		return "plot2";
	}
}
