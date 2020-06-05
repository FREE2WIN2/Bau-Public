package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.TestBlockSlave.ChooseTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlockSlave;
import de.AS.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.TestBlockType;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Type;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.Banner;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.ItemStackCreator;
import de.AS.Bau.utils.Scheduler;

public class TestBlockEditor {

	/*
	 * This class manage everything about the TestBlock-Editor ingame
	 * 
	 * 
	 */

	private Set<ShieldModule> modules;
	private Player owner;
	private ShieldPosition choosedPosition;
	private Facing facing;
	private int tier;

	public TestBlockEditor(Player owner) {
		this.modules = new HashSet<>();
		this.choosedPosition = null;
		this.owner = owner;
	}

	public void openMainInv() {
		Inventory inv = Bukkit.createInventory(null, 54, "§7TestBlock-§6Editor");
		int[] woolPos = { 10, 11, 12, 13, 14, 19, 23, 28, 32, 37, 38, 39, 40, 41 };
		/* Wool */
		ItemStack wool = ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, " ");
		for (int i : woolPos) {
			inv.setItem(i, wool);
		}
		/* Components */
		for (ShieldPosition pos : ShieldPosition.values()) {
			inv.setItem(pos.getPositionInGUI(), pos.getPlaceholderItem(owner));
		}
		/* Modules */
		for (ShieldModule module : modules) {
			inv.setItem(module.getPosition().getPositionInGUI(), module.getType().getItem());
		}
		/* Other Actions */
		inv.setItem(17, ItemStackCreator.createNewItemStack(Material.WOODEN_AXE,
				StringGetterBau.getString(owner, "tbs_editor_mainInv_save")));
		inv.setItem(35, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL,
				StringGetterBau.getString(owner, "tbs_editor_mainInv_show")));
		inv.setItem(53, ItemStackCreator.createNewItemStack(Material.BARRIER,
				StringGetterBau.getString(owner, "tbs_editor_mainInv_reset")));

		owner.openInventory(inv);
	}

	public void openChooseTypeInv() {
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(owner, "tbs_editor_chooseTypeInv"));
		inv.setItem(1, ShieldType.MASSIVE.getItem());
		inv.setItem(2, ShieldType.SAND.getItem());
		inv.setItem(3, ShieldType.SPIKE.getItem());
		inv.setItem(5, ShieldType.ARTOX.getItem());
		inv.setItem(6, ShieldType.ARTILLERY.getItem());
		inv.setItem(7, ShieldType.BACKSTAB.getItem());
		owner.openInventory(inv);
	}

	public void openFacingInv() {
		ItemStack isN = Banner.N.create(DyeColor.WHITE, DyeColor.BLACK,
				StringGetterBau.getString(owner, "facingNorth"));
		// süden
		ItemStack isS = Banner.S.create(DyeColor.WHITE, DyeColor.BLACK,
				StringGetterBau.getString(owner, "facingSouth"));
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(owner, "tbs_editor_facingInv"));
		inv.setItem(2, isN);
		inv.setItem(6, isS);
		owner.openInventory(inv);
	}

	public void openTierInv() {
		String inventoryName = StringGetterBau.getString(owner, "tbs_editor_tierInv");
		Inventory inv = Bukkit.createInventory(null, 9, inventoryName);
		inv.setItem(2, Banner.ONE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier I"));
		inv.setItem(4, Banner.TWO.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier II"));
		inv.setItem(6, Banner.THREE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier III/IV"));
		owner.openInventory(inv);
	}

	public void addModule(ShieldModule module) {
		modules.add(module);
	}

	public void addModule(ShieldType type) {
		if (choosedPosition == null || type == null) {
			return;
		}
		modules.add(new ShieldModule(type, choosedPosition));
		choosedPosition = null;
	}

	public void setChoosedPosition(ShieldPosition choosedPosition) {
		this.choosedPosition = choosedPosition;
	}

	public void startSave() {
		// TODO Auto-generated method stub
		/* First: Visualize */

		/* Then start normel TBS to save */
	}

	public void startVisualize() {
		openFacingInv();

		/* Open Tier and Facing Inv */
		/* Then visualize all compnents */
		/* Saving into tbs undo the full region! */
	}

	public void visualize() {

		/* Save bigger region to UndoManager */
		TestBlockSlave slave = TestBlockSlaveCore.getSlave(owner);
		Region rg = calcUndoRegion();
		slave.getUndoManager().addUndo(rg, CoordGetter.locToVec(owner.getLocation()),
				BukkitAdapter.adapt(owner.getWorld()));

		/* Paste classic TB (without saving!) */
		ChooseTestBlock tb = new ChooseTestBlock();
		tb.setTestBlockType(TestBlockType.DEFAULT);
		tb.setFacing(facing).setTier(tier).setType(Type.NORMAL);
		slave.pasteBlock(tb, false);

		/* visualizing single modules */
		String plotID = WorldGuardHandler.getPlotId(owner.getLocation());
		World world = owner.getWorld();
		Iterator<ShieldModule> iter = modules.iterator();
		Scheduler scheduler = new Scheduler();

		scheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				if (iter.hasNext()) {
					iter.next().visualize(plotID, world, tier, facing);
				} else {
					scheduler.cancel();
				}

			}
		}, 0, 1));

	}

	private Region calcUndoRegion() {
		Region normal = TestBlockSlaveCore.getTBRegion(tier, WorldGuardHandler.getPlotId(owner.getLocation()), facing);
		System.out.println("before: " + normal.getMinimumPoint() + " " + normal.getMaximumPoint());
		int shieldSize = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
		BlockVector3 min = BlockVector3.at(-shieldSize, 0, -shieldSize);
		BlockVector3 max = BlockVector3.at(shieldSize, 0, shieldSize);
		try {
			normal.expand(min, max);
		} catch (RegionOperationException e) {
			e.printStackTrace();
		}
		System.out.println("after: " + normal.getMinimumPoint() + " " + normal.getMaximumPoint());
		return normal;
	}

	public void reset() {
		modules = new HashSet<>();
		choosedPosition = null;
		openMainInv();
	}

	public void setFacing(Facing facing) {
		this.facing = facing;
	}

	public void setTier(int tier) {
		this.tier = tier;
	}
}
