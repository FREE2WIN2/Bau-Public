package net.wargearworld.bau.tools.testBlockSlave;

import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.config.Sizes;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.CoordGetter;
import net.wargearworld.bau.world.plot.Plot;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.ITestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.TestBlockType;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Type;
import net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.TestBlockEditorCore;
import net.wargearworld.bau.utils.Banner;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;

public class TestBlockSlaveCore implements CommandExecutor, Listener {

	private static TestBlockSlaveCore instance;


	@Override
	public boolean onCommand(CommandSender sender, Command cmds, String string, String[] args) {
		Player p = (Player) sender;
		BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
		TestBlockSlave slave = bauPlayer.getTestBlockSlave();
		if (args.length == 0) {
			slave.openGUI();
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("last")) {
				slave.pasteBlock(slave.getlastTestBlock(), slave.getLastFacing(),true);
				return true;
			} else if (args[0].equalsIgnoreCase("undo")) {
				slave.undo();
				return true;
			} else {
				return false;
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("confirmdelete")) {
				String[] args1 = args[1].split("_");
				if (args1.length != 3) {
					return false;
				}
				if (!args1[0].equals(p.getUniqueId().toString())) {
					return false;
				}
				slave.deleteTestBlock(Integer.parseInt(args1[1]), args1[2]);
				return true;
			} else if (args[0].equals("confirmRegion") && args[1].equals(p.getUniqueId().toString())) {
				slave.savingNewTBName();
				return true;
			} else if (args[0].equals("confirmRegionCancel") && args[1].equals(p.getUniqueId().toString())) {
				slave.cancelSave();
				return true;
			}
			return false;
		} else if (args.length == 3) {
			// /tbs <Tier> <Richtung> <normal?>
			// /tbs 1 n n
			ChooseTestBlock chooseTB = new ChooseTestBlock();
			chooseTB.setTestBlockType(TestBlockType.DEFAULT);
			if (args[0].equals("1") || args[0].equals("2") || args[0].equals("3") || args[0].equals("4")) {
				if (args[1].equals("4")) {
					chooseTB.setTier(3);
				} else {
					chooseTB.setTier(Integer.parseInt(args[0]));
				}
				if (args[1].equalsIgnoreCase("n") || args[1].equalsIgnoreCase("s")) {
					chooseTB.setFacing(Facing.getByShort(args[1]));
				} else {
					return false;
				}
				if (args[2].equalsIgnoreCase("N") || args[2].equalsIgnoreCase("S") || args[2].equalsIgnoreCase("R")
						|| args[2].equalsIgnoreCase("F")) {
					if (args[2].equalsIgnoreCase("R")) {
						chooseTB.setType(Type.FRAME);
					} else {
						chooseTB.setType(Type.fromString(args[2]));
					}
				} else {
					return false;
				}
				slave.pasteBlock(chooseTB.getTestBlock(), chooseTB.getFacing(),true);
				/* paste */
				return true;
			}

		} //else if (args.length == 4) {
//			if (!args[0].equalsIgnoreCase("save")) {
//				return false;
//			}
//
//			/* tbs save <name> <tier> <facing> */
//
//			String name = args[1];
//			Integer tier = Integer.parseInt(args[2]);
//			Facing facing = Facing.valueOf(args[3].toUpperCase());
//			slave.addNewCustomTestBlock(tier, name, facing, WorldGuardHandler.getPlotId(p.getLocation()));// missing
//																												// tier!
//			return true;
//		}
		return false;
		
	}

	@EventHandler
	public void onInventoryclick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			return;
		}
		Player p = (Player) event.getWhoClicked();
		BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
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
		/* Main Inventory */
		if (invName.equals(MessageHandler.getInstance().getString(p, "testBlockSklaveMainInv"))
				&& event.getClickedInventory().equals(pInv)) {
			event.setCancelled(true);
			MainInventory(p, clicked);
			/* Facing inv (saving and pasting!) */
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "testBlockSklaveFacingInv"))
				&& event.getClickedInventory().equals(pInv)) {
			event.setCancelled(true);
			facingInv(p, clicked);
			/* Type Inv(Saving, default paste) */
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "testBlockSklaveTypeInv"))
				&& event.getClickedInventory().equals(pInv)) {
			event.setCancelled(true);
			typeInv(p, clicked);

			/* TB-Manager */
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_tbManagerInv").replace("%r", p.getName()))) {
			if (clicked.getItemMeta().getDisplayName().equals(" ")) {
				/* empty Banner */
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			tbManagerInv(p, clicked);
			/* Fav inv */
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_gui_addFavoriteInv"))) {
			bauPlayer.getTestBlockSlave().setTestBlockToFavorite(clicked);
			p.closeInventory();
			event.setCancelled(true);
			/* Tier inv to save */
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_gui_tierInv"))) {
			tierInv(p, clicked);
			event.setCancelled(true);
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_tbManagerActionInv", p.getName()))) {
			chooseActionToCustomTB(p, clicked);
		} else if (invName.equals(MessageHandler.getInstance().getString(p, "tbs_gui_anvilTitle"))) {
			if (event.getSlotType().equals(SlotType.RESULT)) {
				p.closeInventory();
				bauPlayer.getTestBlockSlave().saveNewCustomTB(((AnvilInventory) event.getInventory()).getRenameText());
			}

		}

	}

	private void tierInv(Player p, ItemStack clicked) {
		String clickedName = clicked.getItemMeta().getDisplayName();
		ChooseTestBlock chooseTB = BauPlayer.getBauPlayer(p).getTestBlockSlave().getChooseTB();
		switch (clickedName) {
		case "§rTier I":
			chooseTB.setTier(1);
			break;
		case "§rTier II":
			chooseTB.setTier(2);
			break;
		case "§rTier III/IV":
			chooseTB.setTier(3);
			break;
		}
		p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
	}

	private void tbManagerInv(Player p, ItemStack clicked) {
		/* open Next Inv */
		BauPlayer.getBauPlayer(p).getTestBlockSlave().startNewChoose(clicked);
		p.openInventory(TestBlockSlaveGUI.tbManagerActionInv(p));
	}

	private void chooseActionToCustomTB(Player p, ItemStack clicked) {
		TestBlockSlave slave = BauPlayer.getBauPlayer(p).getTestBlockSlave();
		ITestBlock tb = slave.getChooseTB().getTestBlock();
		String clickedName = clicked.getItemMeta().getDisplayName();
		String south = MessageHandler.getInstance().getString(p, "facingSouth");
		String north = MessageHandler.getInstance().getString(p, "facingNorth");
		ChooseTestBlock chooseTB = slave.getChooseTB();

		if (clicked.getType().equals(Material.BARRIER)) {
			/* Delete */
			String name = tb.getName();
			JsonCreater deleteBegin = new JsonCreater(
					Main.prefix + MessageHandler.getInstance().getString(p, "tbs_deleteCustomBlock")
							.replaceFirst("%r", tb.getTier() + "").replaceFirst("%r", name));
			JsonCreater deleteClickConfirm = new JsonCreater(
					MessageHandler.getInstance().getString(p, "tbs_deleteCustomBlockConfirm"));
			deleteClickConfirm.addClickEvent("/tbs confirmdelete " + p.getUniqueId() + "_" + tb.getTier() + "_" + name,
					ClickAction.RUN_COMMAND);
			deleteClickConfirm.addHoverEvent(
					MessageHandler.getInstance().getString(p, "tbs_deleteCustomBlockConfirm_hover").replace("%r", name));
			deleteBegin.addJson(deleteClickConfirm).send(p);
			p.closeInventory();
		} else if (clicked.equals(Banner.PLUS.create(DyeColor.WHITE, DyeColor.BLACK,
				MessageHandler.getInstance().getString(p, "tbs_gui_tbManagerFavorite")))) {
			/* Add to Favorite */
			if (slave.setTestBlockToFavorite(tb)) {
				slave.showTBManager();// aktualisieren
			}
		} else if (clicked.equals(Banner.MINUS.create(DyeColor.WHITE, DyeColor.BLACK,
				MessageHandler.getInstance().getString(p, "tbs_gui_tbManagerFavoriteRemove")))) {
			/* Remove from Favorites */
			if (slave.removeFavorite(tb)) {
				slave.showTBManager();// aktualisieren
			}

		} else if (north.equals(clickedName)) {
			chooseTB.setFacing(Facing.NORTH);
			slave.pasteBlock(chooseTB,true);
			p.closeInventory();
		} else if (south.equals(clickedName)) {
			chooseTB.setFacing(Facing.SOUTH);
			slave.pasteBlock(chooseTB, true);
			p.closeInventory();
		}
	}



	private void MainInventory(Player p, ItemStack clicked) {
		String close = MessageHandler.getInstance().getString(p, "tbs_gui_close");
		String lastPaste = MessageHandler.getInstance().getString(p, "tbs_gui_lastPaste");
		String clickedName = clicked.getItemMeta().getDisplayName();
		TestBlockSlave slave = BauPlayer.getBauPlayer(p).getTestBlockSlave();
		switch (clickedName) {
		case "§rTier I":
			slave.startNewChoose(TestBlockType.DEFAULT, 1);
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			return;
		case "§rTier II":
			slave.startNewChoose(TestBlockType.DEFAULT, 2);
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			return;
		case "§rTier III/IV":
			slave.startNewChoose(TestBlockType.DEFAULT, 3);
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			return;
		}
		if (clickedName.equals(close)) {
			p.closeInventory();
		} else if (clickedName.equals(lastPaste)) {

			slave.pasteBlock(slave.getlastTestBlock(), slave.getLastFacing(),true);
			p.closeInventory();
		} else if (clickedName.equals(MessageHandler.getInstance().getString(p, "tbs_gui_tbManager"))) {
			slave.showTBManager();
		} else if (clicked.getType().equals(Material.WHITE_BANNER)) {
			/* All Banner are White banner! */

			if (clickedName.equalsIgnoreCase(MessageHandler.getInstance().getString(p, "tbs_gui_addFavorites"))) {
				/* Add Favorites */
				slave.openAddFavoriteInv();
			} else {
				/* Paste Favorite */
				slave.startNewChoose(clicked);
				p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			}

		} else if (clicked.getType().equals(Material.WOODEN_AXE)) {
			/* save new TB */

			/* Richtung, */

			slave.startSavingNewTB();

		} else if (clickedName.equals("§6Editor")) {
			TestBlockEditorCore.getEditor(p).openMainInv();
		}
	}

	private void facingInv(Player p, ItemStack clicked) {
		String south = MessageHandler.getInstance().getString(p, "facingSouth");
		String north = MessageHandler.getInstance().getString(p, "facingNorth");
		String clickedName = clicked.getItemMeta().getDisplayName();
		TestBlockSlave slave = BauPlayer.getBauPlayer(p).getTestBlockSlave();
		ChooseTestBlock chooseTB = slave.getChooseTB();
		if (north.equals(clickedName)) {
			chooseTB.setFacing(Facing.NORTH);
		} else if (south.equals(clickedName)) {
			chooseTB.setFacing(Facing.SOUTH);
		}
		if (chooseTB.getTestBlockType().equals(TestBlockType.DEFAULT)) {
			p.openInventory(TestBlockSlaveGUI.schildRahmenNormalInventory(p));
		} else if (chooseTB.getTestBlockType().equals(TestBlockType.NEW)) {
			p.openInventory(TestBlockSlaveGUI.schildNormalInventory(p));
		} else {
			p.closeInventory();
			slave.pasteBlock(chooseTB,true);
		}

	}

	private void typeInv(Player p, ItemStack clicked) {
		String normal = "§rNormal";
		String frame = MessageHandler.getInstance().getString(p, "frame");
		String shield = MessageHandler.getInstance().getString(p, "shield");
		String clickedName = clicked.getItemMeta().getDisplayName();
		TestBlockSlave slave = BauPlayer.getBauPlayer(p).getTestBlockSlave();
		ChooseTestBlock chooseTB = slave.getChooseTB();

		if (clickedName.equals(normal)) {
			chooseTB.setType(Type.NORMAL);
		} else if (clickedName.equals(frame)) {
			chooseTB.setType(Type.FRAME);
		} else if (clickedName.equals(shield)) {
			chooseTB.setType(Type.SHIELDS);
		}

		if (chooseTB.getTestBlockType().equals(TestBlockType.DEFAULT)) {
			/* Default TB */

			p.closeInventory();

			slave.pasteBlock(chooseTB.getTestBlock(), chooseTB.getFacing(),true);

		} else {
			/* Saving new TB */
			p.closeInventory();
			slave.showParticle();
		}

	}

	public static TestBlockSlaveCore getInstance() {
		if (instance == null) {
			instance = new TestBlockSlaveCore();
		}
		return instance;
	}

	public static Region getTBRegion(int tier, Plot plot, Facing facing) {
		BlockVector3 middle = CoordGetter.getMiddleRegionTB(plot, facing);
		BlockVector3 sizes = CoordGetter.getMaxSizeOfBlock(tier);

		/* If North -> middle.z = min.z ! */

		BlockVector3 min;
		BlockVector3 max;
		if (facing == Facing.NORTH) {
			min = middle.subtract(sizes.divide(2).getX(), 0, -1);
			max = middle.add(sizes.divide(2).getX(), sizes.getY() - 1, sizes.getZ());
		} else {
			min = middle.subtract(sizes.divide(2).getX(), 0, sizes.getZ());
			max = middle.add(sizes.divide(2).getX(), sizes.getY() - 1, -1);
		}
		return new CuboidRegion(min, max);
	}

	public static int getMaxShieldSizeOfTier(int tier) {
		Sizes sizes = BauConfig.getInstance().getSize(tier);
		if(sizes == null)
			return 0;
		return sizes.getShields();
	}
}
