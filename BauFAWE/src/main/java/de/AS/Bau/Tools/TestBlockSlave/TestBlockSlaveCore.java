package de.AS.Bau.Tools.TestBlockSlave;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.DefaultTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.TestBlock;
import de.AS.Bau.utils.Facing;

public class TestBlockSlaveCore implements CommandExecutor, Listener {

	public static HashSet<Player> playerBlockedDelete = new HashSet<>();
	public HashMap<UUID, String> playersCurrentSelection = new HashMap<>();
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
			} else {
				
				return false;
			}

		}else if(args.length == 4) {
			if(!args[0].equalsIgnoreCase("save")) {
				return false;
			}
			// tbs save <name> <tier> <facing>
			String name = args[1];
			Integer tier = Integer.parseInt(args[2]);
			Facing facing = Facing.valueOf(args[3].toUpperCase());
			getSlave(p).addNewCustomTestBlock(tier, name, facing);//missing tier!
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
			if (clicked.getType().equals(Material.NETHER_STAR) || clicked.getType().equals(Material.WOODEN_HOE)) {
				return;
			}
			if (clicked.hasItemMeta()) {
				if (!pInv.getType().equals(InventoryType.CREATIVE)) {
					if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveMainInv"))
							&& event.getClickedInventory().equals(pInv)) {

						event.setCancelled(true);
						MainInventory(p, pInv, clicked);
					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveFacingInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						facingInv(p, pInv, clicked);

					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveTypeInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						typeInv(p, pInv, clicked);

					}

				}
			}
		}

	}

	private void MainInventory(Player p, Inventory pInv, ItemStack clicked) {
		String close = StringGetterBau.getString(p, "tbs_gui_close");
		String lastPaste = StringGetterBau.getString(p, "tbs_gui_lastPaste");
		String clickedName = clicked.getItemMeta().getDisplayName();
		switch (clickedName) {
		case "§rTier I":
			playersCurrentSelection.put(p.getUniqueId(), "T1_");
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			break;
		case "§rTier II":
			playersCurrentSelection.put(p.getUniqueId(), "T2_");
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			break;
		case "§rTier III/IV":
		case "§rTier IV":
			playersCurrentSelection.put(p.getUniqueId(), "T3_");
			p.openInventory(TestBlockSlaveGUI.richtungsInventory(p));
			break;
		}
		if (clickedName.equals(close)) {
			p.closeInventory();
		} else if (clickedName.equals(lastPaste)) {
			TestBlockSlave slave = getSlave(p);
			slave.pasteBlock(slave.getlastTestBlock(), slave.getLastFacing());
		}

	}

	private void facingInv(Player p, Inventory pInv, ItemStack clicked) {
		String south = StringGetterBau.getString(p, "facingSouth");
		String north = StringGetterBau.getString(p, "facingNorth");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String current = playersCurrentSelection.get(p.getUniqueId());
		if (north.equals(clickedName)) {
			current += "N_";
		} else if (south.equals(clickedName)) {
			current += "S_";
		}
		playersCurrentSelection.put(p.getUniqueId(), current);
		p.openInventory(TestBlockSlaveGUI.schildRahmenNormalInventory(p));
	}

	private void typeInv(Player p, Inventory pInv, ItemStack clicked) {
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
		p.closeInventory();
		
		if(current.contains("_S_")) {
			getSlave(p).pasteBlock(getDefaultBlock(current.replace("_S_", "_N_")), Facing.SOUTH);
		}else {
			getSlave(p).pasteBlock(getDefaultBlock(current), Facing.NORTH);
		}
		
	}
	
	public static TestBlockSlave getSlave(Player p) {
		if (!playersTestBlockSlave.containsKey(p.getUniqueId())) {
			playersTestBlockSlave.put(p.getUniqueId(), new TestBlockSlave(p));
		}
		return playersTestBlockSlave.get(p.getUniqueId());
	}

	private TestBlock getDefaultBlock(String current) {
		return defaultTestBlocks.get(current);
	}

	public static void generateDefaultTestBlocks() {
		// TODO mit DB?

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
