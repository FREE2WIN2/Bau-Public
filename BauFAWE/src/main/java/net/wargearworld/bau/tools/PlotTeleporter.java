package net.wargearworld.bau.tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.utils.CoordGetter;
import net.wargearworld.bau.utils.ItemStackCreator;

public class PlotTeleporter implements Listener {

	public static void openInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, MessageHandler.getInstance().getString(p, "gui_teleporter"));
		// 2pearls
		int countOfNormalPlots = CoordGetter.getConfigOfWorld(p.getWorld().getName()).getInt("countplots");
		int index = 0;
		for (int i = 1; i <= countOfNormalPlots; i++) {
			ItemStack teleportNormal = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
					MessageHandler.getInstance().getString(p, "teleportNormalPlot", "" + i));
			teleportNormal.setAmount(i);
			inv.setItem(index, teleportNormal);
			index++;
		}

		ConfigurationSection section = CoordGetter.getConfigOfWorld(p.getWorld().getName()).getConfigurationSection("counttestblockspertier");
		int[] countOfTestBlocks = new int[3];
		for(int i = 1;i<=3;i++) {
			countOfTestBlocks[i-1] = section.getInt(""+i);
		}
		index = 9;
		for (int tier = 1; tier <= 3; tier++) {
			for (int i = 1; i <= countOfTestBlocks[tier-1]; i++) {
				ItemStack teleportTest = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
						MessageHandler.getInstance().getString(p, "teleportTestPlot", "" + i, "" + tier));
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
		if (!event.getView().getTitle().equals(MessageHandler.getInstance().getString(p, "gui_teleporter"))) {
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
			ConfigurationSection section = CoordGetter.getConfigOfWorld(p.getWorld().getName()).getConfigurationSection("counttestblockspertier");
			int[] countOfTestBlocks = new int[3];
			for(int i = 1;i<=3;i++) {
				countOfTestBlocks[i-1] = section.getInt(""+i);
			}
			for (int tier = 1; tier <= 3; tier++) {
				for (int i = 1; i <= countOfTestBlocks[tier-1]; i++) {
					String name = MessageHandler.getInstance().getString(p, "teleportTestPlot", "" + i, "" + tier);
					if (item.getItemMeta().getDisplayName().equals(name)) {
						return "t" + tier + "," + i;
					}
				}
			}

		}
		return "plot2";
	}
}
