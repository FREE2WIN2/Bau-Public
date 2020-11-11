package net.wargearworld.bau.tools;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.bau.world.plot.PlotType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.utils.ItemStackCreator;

public class PlotTeleporter {

	public static void openInv(Player p) {
		GUI gui = new ChestGUI(18, MessageHandler.getInstance().getString(p, "gui_teleporter"));
		// 2pearls

		BauWorld world = WorldManager.get(p.getWorld());

		int countOfNormalPlots = world.getAmountOfPlots(PlotType.DEFAULT);
		int index = 0;
		for (int i = 1; i <= countOfNormalPlots; i++) {
			ItemStack teleportNormal = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
					MessageHandler.getInstance().getString(p, "teleportNormalPlot", "" + i));
			teleportNormal.setAmount(i);
			gui.setItem(index, new DefaultItem(teleportNormal,s->{
				p.teleport(getLocation(p, p.getWorld(), s.getClicked()));
				ScoreBoardBau.cmdUpdate(p);}));
			index++;
		}

		int[] countOfTestBlocks = {0,0,0};
		for(Plot plot:world.getPlots(PlotType.TEST)){
			int tier = world.getTemplate().getTier(plot.getId());
			countOfTestBlocks[tier-1] = countOfTestBlocks[tier-1] + 1;
		}
		index = 9;
		for (int tier = 1; tier <= 3; tier++) {
			for (int i = 1; i <= countOfTestBlocks[tier-1]; i++) {
				ItemStack teleportTest = ItemStackCreator.createNewItemStack(Material.ENDER_EYE,
						MessageHandler.getInstance().getString(p, "teleportTestPlot", "" + i, "" + tier));
				teleportTest.setAmount(tier);
				gui.setItem(index, new DefaultItem(teleportTest,s->{
					p.teleport(getLocation(p, p.getWorld(), s.getClicked()));
					ScoreBoardBau.cmdUpdate(p);}));
				index++;
				}
			}
		gui.open(p);
	}


	private static Location getLocation(Player p, World w, ItemStack item) {
		return WorldManager.get(w).getPlot(getPlotId(p,item)).getTeleportPoint();
	}

	private static String getPlotId(Player p, ItemStack item) {
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
			int[] countOfTestBlocks = {0,0,0};
			BauWorld world = WorldManager.get(p.getWorld());
			for(Plot plot:world.getPlots(PlotType.TEST)){
				int tier = world.getTemplate().getTier(plot.getId());
				countOfTestBlocks[tier-1] = countOfTestBlocks[tier-1] + 1;
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
