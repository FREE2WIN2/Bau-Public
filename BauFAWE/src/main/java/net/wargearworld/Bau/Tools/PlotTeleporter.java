package net.wargearworld.Bau.Tools;

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

import net.wargearworld.Bau.StringGetterBau;
import net.wargearworld.Bau.Scoreboard.ScoreBoardBau;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.ItemStackCreator;

public class PlotTeleporter implements Listener {

	public static void openInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, StringGetterBau.getString(p, "gui_teleporter"));
		// 2pearls
		int countOfNormalPlots = CoordGetter.getConfigOfWorld(p.getWorld().getName()).getInt("countplots");
		int index = 0;
		for (int i = 1; i <= countOfNormalPlots; i++) {
			ItemStack teleportNormal = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
					StringGetterBau.getString(p, "teleportNormalPlot", "" + i));
			teleportNormal.setAmount(i);
			inv.setItem(index, teleportNormal);
			index++;
		}


			int countOftestPlotsPerTier = CoordGetter.getConfigOfWorld(p.getWorld().getName()).getInt("counttestblockspertier");
			index = 9;
			for (int tier = 1; tier <= 3; tier++) {
				for (int i = 1; i <= countOftestPlotsPerTier; i++) {
					ItemStack teleportTest = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
							StringGetterBau.getString(p, "teleportTestPlot", "" + i, "" + tier));
					teleportTest.setAmount(tier);
					inv.setItem(index, teleportTest);
					index++;
				}
			}
		

		p.openInventory(inv);
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			return;
		}
		if (event.getSlotType() == SlotType.OUTSIDE) {
			return;
		}
		ItemStack item = event.getCurrentItem();
		if (item == null) {
			return;
		}
		if (!item.hasItemMeta()) {
			return;
		}

		Player p = (Player) event.getWhoClicked();
		if (!event.getView().getTitle().equals(StringGetterBau.getString(p, "gui_teleporter"))) {
			return;
		}
		event.setCancelled(true);
		p.teleport(getLocation(p, p.getWorld(), item));
		ScoreBoardBau.cmdUpdate(p);
	}

	private Location getLocation(Player p, World w, ItemStack item) {
		return CoordGetter.getTeleportLocation(w, getPlotId(p, item));
	}

	private String getPlotId(Player p, ItemStack item) {
		if (item.getType() == Material.ENDER_PEARL) {
			switch (item.getAmount()) {
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
		} else if (item.getType() == Material.ENDER_EYE) {			
			int countOftestPlotsPerTier = CoordGetter.getConfigOfWorld(p.getWorld().getName()).getInt("counttestblockspertier");
			for (int tier = 1; tier <= 3; tier++) {
				for (int i = 1; i <= countOftestPlotsPerTier; i++) {
					String name = StringGetterBau.getString(p, "teleportTestPlot", "" + i, "" + tier);
					if (item.getItemMeta().getDisplayName().equals(name)) {
						return "plottier" + tier + "test" + i;
					}
				}
			}

		}
		return "plot2";
	}
}
