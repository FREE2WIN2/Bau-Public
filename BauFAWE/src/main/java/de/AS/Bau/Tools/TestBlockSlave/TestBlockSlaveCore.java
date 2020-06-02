package de.AS.Bau.Tools.TestBlockSlave;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.DefaultTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.TestBlock;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.Banner;
import de.AS.Bau.utils.ClickAction;
import de.AS.Bau.utils.Facing;
import de.AS.Bau.utils.JsonCreater;

public class TestBlockSlaveCore implements CommandExecutor, Listener {

	public static HashSet<Player> playerBlockedDelete = new HashSet<>();
	public static HashMap<UUID, String> playersCurrentSelection = new HashMap<>();
	public static HashMap<UUID, TestBlockSlave> playersTestBlockSlave = new HashMap<>();
	public static HashMap<String, DefaultTestBlock> defaultTestBlocks = new HashMap<>();
	private static TestBlockSlaveCore instance;

	@Override
	public boolean onCommand(CommandSender sender, Command cmds, String string, String[] args) {
		Player p = (Player) sender;
		if (args.length == 0) {
			getSlave(p).openGUI();
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("last")) {
				TestBlockSlave slave = getSlave(p);
				slave.pasteBlock(slave.getlastTestBlock(), slave.getLastFacing());
				return true;
			} else if (args[0].equalsIgnoreCase("undo")) {
				getSlave(p).undo();
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
				getSlave(p).deleteTestBlock(Integer.parseInt(args1[1]), args1[2]);
				return true;
			}
			return false;
		} else if (args.length == 3) {
			// /tbs <Tier> <Richtung> <normal?>
			// /tbs 1 n n
			String tier = "T";
			String Richtung = "";
			String typ = "";
			if (args[0].equals("1") || args[0].equals("2") || args[0].equals("3") || args[0].equals("4")) {
				if (args[1].equals("4")) {
					tier = "T3";
				} else {
					tier = tier + args[0];
				}
				if (args[1].equalsIgnoreCase("n") || args[1].equalsIgnoreCase("s")) {
					Richtung = args[1].toUpperCase();
				} else {
					return false;
				}
				if (args[2].equalsIgnoreCase("N") || args[2].equalsIgnoreCase("S") || args[2].equalsIgnoreCase("R")
						|| args[2].equalsIgnoreCase("F")) {
					if (args[2].equalsIgnoreCase("R")) {
						typ = "F";
					} else {
						typ = args[2].toUpperCase();
					}
				} else {
					return false;
				}
				String auswahl = tier + "_N_" + typ;
				getSlave(p).pasteBlock(getDefaultBlock(auswahl), Facing.valueOf(Richtung));
				/* paste */

				return true;
			} else if (args[0].equals("confirmRegion") && args[1].equals(p.getUniqueId().toString())) {
				// /tbs confirmRegion " + owner.getUniqueId() + " " + currentSelection
				playersCurrentSelection.put(p.getUniqueId(), "");
				getSlave(p).savingNewTBName(Integer.parseInt(args[2].split("_")[2]));
				return true;
			}

		} else if (args.length == 4) {
			if (!args[0].equalsIgnoreCase("save")) {
				return false;
			}

			/* tbs save <name> <tier> <facing> */

			String name = args[1];
			Integer tier = Integer.parseInt(args[2]);
			Facing facing = Facing.valueOf(args[3].toUpperCase());
			getSlave(p).addNewCustomTestBlock(tier, name, facing, WorldGuardHandler.getPlotId(p.getLocation()));// missing tier!
			return true;
		}
		return false;
	}

	@EventHandler
	public void onInventoryclick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Inventory pInv = p.getOpenInventory().getTopInventory();
		String invName = p.getOpenInventory().getTitle();
		ItemStack clicked = event.getCurrentItem();
		if (clicked != null) {
			if (clicked.hasItemMeta()) {
				if (!pInv.getType().equals(InventoryType.CREATIVE)) {
					/* Main Inventroy */
					if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveMainInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						MainInventory(p, clicked);
						/* Facing inv (saving and pasting!) */
					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveFacingInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						facingInv(p, clicked);
						/* Type Inv(Saving, default paste) */
					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveTypeInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						typeInv(p, clicked);

						/* TB-Manager */
					} else if (invName
							.equals(StringGetterBau.getString(p, "tbs_tbManagerInv").replace("%r", p.getName()))) {
						if (clicked.getItemMeta().getDisplayName().equals(" ")) {
							/* empty Banner */
							event.setCancelled(true);
							return;
						}
						if (event.getCursor().getType() != Material.AIR) {
							event.setCancelled(true);
							tbManagerInv(p, clicked, event.getCursor());
						}
						/* Fav inv */
					} else if (invName.equals(StringGetterBau.getString(p, "tbs_gui_addFavoriteInv"))) {
						getSlave(p).setTestBlockToFavorite(clicked);
						p.closeInventory();
						event.setCancelled(true);
						/* Tier inv to save */
					} else if (invName.equals(StringGetterBau.getString(p, "tbs_gui_tierInv"))) {
						tierInv(p, clicked);
						event.setCancelled(true);
					} else if (pInv.getType().equals(InventoryType.ANVIL)) {
						if (event.getSlotType().equals(SlotType.RESULT)) {
							p.closeInventory();
							getSlave(p).saveNewCustomTB(((AnvilInventory) event.getInventory()).getRenameText());
						}
					}

				}
			}
		} else {
			if(event.getClickedInventory() == null) {
				return;
			}
			if (event.getClickedInventory().equals(pInv)) {
				event.setCancelled(true);
			}
		}

	}

	private void tierInv(Player p, ItemStack clicked) {
		String clickedName = clicked.getItemMeta().getDisplayName();
		String current = playersCurrentSelection.get(p.getUniqueId());
		switch (clickedName) {
		case "§rTier I":
			current += "1_";
			break;
		case "§rTier II":
			current += "2_";
			break;
		case "§rTier III/IV":
			current += "3_";
			break;
		}
		playersCurrentSelection.put(p.getUniqueId(), current);
		p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
	}

	private void tbManagerInv(Player p, ItemStack clicked, ItemStack cursor) {
		// -> open Richtungsinv und dann pasten!
		// oder löschen?

		/* TestBlock? oder anderes */
		if (clicked.getType().equals(Material.WHITE_WOOL)) {
			/* init paste */
			playersCurrentSelection.put(p.getUniqueId(), cursor.getItemMeta().getDisplayName()); // Displayname ==
																									// TestBlockName
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
		} else if (clicked.getType().equals(Material.BARRIER)) {
			/* Delete */
			TestBlock tb = getSlave(p).getBlockOutOfBanner(cursor);
			String name = tb.getName();
			JsonCreater deleteBegin = new JsonCreater(
					Main.prefix + StringGetterBau.getString(p, "tbs_deleteCustomBlock")
							.replaceFirst("%r", tb.getTier() + "").replaceFirst("%r", name));
			JsonCreater deleteClickConfirm = new JsonCreater(
					StringGetterBau.getString(p, "tbs_deleteCustomBlockConfirm"));
			deleteClickConfirm.addClickEvent("/tbs confirmdelete " + p.getUniqueId() + "_" + tb.getTier() + "_" + name,
					ClickAction.RUN_COMMAND);
			deleteClickConfirm.addHoverEvent(
					StringGetterBau.getString(p, "tbs_deleteCustomBlockConfirm_hover").replace("%r", name));
			deleteBegin.addJson(deleteClickConfirm).send(p);
			p.closeInventory();
		} else if (clicked.equals(Banner.PLUS.create(DyeColor.WHITE, DyeColor.BLACK,
				StringGetterBau.getString(p, "tbs_gui_tbManagerFavorite")))) {
			/* Add to Favorite */
			if (getSlave(p).setTestBlockToFavorite(cursor)) {
				getSlave(p).showTBManager();// aktualisieren
			}
		} else if (clicked.equals(Banner.MINUS.create(DyeColor.WHITE, DyeColor.BLACK,
				StringGetterBau.getString(p, "tbs_gui_tbManagerFavoriteRemove")))) {
			/* Remove from Favorites */
			if (getSlave(p).removeFavorite(cursor)) {
				getSlave(p).showTBManager();// aktualisieren
			}

		}

	}

	private void MainInventory(Player p, ItemStack clicked) {
		String close = StringGetterBau.getString(p, "tbs_gui_close");
		String lastPaste = StringGetterBau.getString(p, "tbs_gui_lastPaste");
		String clickedName = clicked.getItemMeta().getDisplayName();
		switch (clickedName) {
		case "§rTier I":
			playersCurrentSelection.put(p.getUniqueId(), "Default_T1_");
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			return;
		case "§rTier II":
			playersCurrentSelection.put(p.getUniqueId(), "Default_T2_");
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			return;
		case "§rTier III/IV":
			playersCurrentSelection.put(p.getUniqueId(), "Default_T3_");
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			return;
		}
		if (clickedName.equals(close)) {
			p.closeInventory();
		} else if (clickedName.equals(lastPaste)) {
			TestBlockSlave slave = getSlave(p);
			slave.pasteBlock(slave.getlastTestBlock(), slave.getLastFacing());
			p.closeInventory();
		} else if (clickedName.equals(StringGetterBau.getString(p, "tbs_gui_tbManager"))) {
			getSlave(p).showTBManager();
		} else if (clicked.getType().equals(Material.WHITE_BANNER)) {
			/* All Banner are White banner! */

			if (clickedName.equalsIgnoreCase(StringGetterBau.getString(p, "tbs_gui_addFavorites"))) {
				/* Add Favorites */
				getSlave(p).openAddFavoriteInv();
			} else {
				/* Paste Favorite */
				playersCurrentSelection.put(p.getUniqueId(), clickedName);
				p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			}

		} else if (clicked.getType().equals(Material.WOODEN_AXE)) {
			/* save new TB */

			/* Richtung, */

			getSlave(p).startSavingNewTB();

		}
	}

	private void facingInv(Player p, ItemStack clicked) {
		String south = StringGetterBau.getString(p, "facingSouth");
		String north = StringGetterBau.getString(p, "facingNorth");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String current = playersCurrentSelection.get(p.getUniqueId());
		if (current.startsWith("Default_T")) {
			if (north.equals(clickedName)) {
				current += "N_";
			} else if (south.equals(clickedName)) {
				current += "S_";
			}
			playersCurrentSelection.put(p.getUniqueId(), current);
			p.openInventory(TestBlockSlaveGUI.schildRahmenNormalInventory(p));
		} else if (current.startsWith("New_TB_")) {
			if (north.equals(clickedName)) {
				current += "N_";
			} else if (south.equals(clickedName)) {
				current += "S_";
			}
			playersCurrentSelection.put(p.getUniqueId(), current);
			p.openInventory(TestBlockSlaveGUI.schildNormalInventory(p));
		} else {
			Facing facing;
			if (north.equals(clickedName)) {
				facing = Facing.NORTH;
			} else {
				facing = Facing.SOUTH;
			}
			p.closeInventory();
			getSlave(p).pasteBlock(current, facing);
			playersCurrentSelection.put(p.getUniqueId(), "");
		}

	}

	private void typeInv(Player p, ItemStack clicked) {
		String normal = "§rNormal";
		String frame = StringGetterBau.getString(p, "frame");
		String shield = StringGetterBau.getString(p, "shield");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String current = playersCurrentSelection.get(p.getUniqueId());

		if (clickedName.equals(normal)) {
			current += "N";
		} else if (clickedName.equals(frame)) {
			current += "F";
		} else if (clickedName.equals(shield)) {
			current += "S";
		}

		if (current.startsWith("Default_T")) {
			/* Default TB */

			p.closeInventory();

			if (current.contains("_S_")) {
				getSlave(p).pasteBlock(getDefaultBlock(current.replace("_S_", "_N_")), Facing.SOUTH);
			} else {
				getSlave(p).pasteBlock(getDefaultBlock(current), Facing.NORTH);
			}
		} else {
			/* Saving new TB */
			p.closeInventory();
			getSlave(p).showParticle(current);

		}

	}

	public static TestBlockSlave getSlave(Player p) {
		if (!playersTestBlockSlave.containsKey(p.getUniqueId())) {
			playersTestBlockSlave.put(p.getUniqueId(), new TestBlockSlave(p));
		}
		return playersTestBlockSlave.get(p.getUniqueId());
	}

	private TestBlock getDefaultBlock(String current) {
		return defaultTestBlocks.get(current.replace("Default_", ""));
	}

	public static void generateDefaultTestBlocks() {
		String[] tiers = new String[3];
		String[] types = new String[3];
		tiers[0] = "T1";
		tiers[1] = "T2";
		tiers[2] = "T3";

		types[0] = "S";
		types[1] = "N";
		types[2] = "F";
		for (String tier : tiers) {
			for (String type : types) {
				String name = tier + "_N_" + type;
				defaultTestBlocks.put(name, new DefaultTestBlock(name));
			}

		}
		System.out.println("Default TB loaded");
	}

	public static TestBlockSlaveCore getInstance() {
		if (instance == null) {
			instance = new TestBlockSlaveCore();
		}
		return instance;
	}

}
