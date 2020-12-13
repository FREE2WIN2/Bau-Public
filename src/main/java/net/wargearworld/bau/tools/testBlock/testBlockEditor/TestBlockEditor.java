package net.wargearworld.bau.tools.testBlock.testBlockEditor;

import java.util.*;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.CoordGetter;
import net.wargearworld.bau.world.plot.Plot;
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

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.testBlock.ChooseTestBlock;
import net.wargearworld.bau.tools.testBlock.TestBlock;
import net.wargearworld.bau.tools.testBlock.TestBlockCore;
import net.wargearworld.bau.tools.testBlock.testBlock.Facing;
import net.wargearworld.bau.tools.testBlock.testBlock.TestBlockType;
import net.wargearworld.bau.tools.testBlock.testBlock.Type;
import net.wargearworld.bau.utils.Banner;
import net.wargearworld.bau.utils.ItemStackCreator;
import net.wargearworld.bau.utils.Scheduler;

public class TestBlockEditor {

	/*
	 * This class manage everything about the TestBlock-Editor ingame
	 * 
	 * 
	 */

	private Set<ShieldModule> modules;
	private UUID owner;
	private ShieldPosition choosedPosition;
	private Facing facing;
	private int tier;
	private boolean save;

	public TestBlockEditor(UUID owner) {
		this.modules = new HashSet<>();
		this.choosedPosition = null;
		this.owner = owner;
		save = false;
	}

	public void openMainInv() {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		Objects.requireNonNull(ownerPlayer);

		Inventory inv = Bukkit.createInventory(null, 54, "§7TestBlock-§6Editor");
		int[] woolPos = { 10, 11, 12, 13, 14, 19, 23, 28, 32, 37, 38, 39, 40, 41 };
		/* Wool */
		ItemStack wool = ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, " ");
		for (int i : woolPos) {
			inv.setItem(i, wool);
		}
		/* Components */
		for (ShieldPosition pos : ShieldPosition.values()) {
			inv.setItem(pos.getPositionInGUI(), pos.getPlaceholderItem(ownerPlayer));
		}
		/* Modules */
		for (ShieldModule module : modules) {
			inv.setItem(module.getPosition().getPositionInGUI(), module.getType().getItem());
		}
		/* Other Actions */
		inv.setItem(17, ItemStackCreator.createNewItemStack(Material.WOODEN_AXE,
				MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_mainInv_save")));
		inv.setItem(35, ItemStackCreator.createNewItemStack(Material.WHITE_WOOL,
				MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_mainInv_show")));
		inv.setItem(53, ItemStackCreator.createNewItemStack(Material.BARRIER,
				MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_mainInv_reset")));

		ownerPlayer.openInventory(inv);
	}

	public void openChooseTypeInv() {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		Objects.requireNonNull(ownerPlayer);

		Inventory inv = Bukkit.createInventory(null, 9, MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_chooseTypeInv"));
		inv.setItem(1, ShieldType.MASSIVE.getItem());
		inv.setItem(2, ShieldType.SAND.getItem());
		inv.setItem(3, ShieldType.SPIKE.getItem());
		inv.setItem(4, ItemStackCreator.createNewItemStack(Material.BARRIER,
				MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_removeType")));
		inv.setItem(5, ShieldType.ARTOX.getItem());
		inv.setItem(6, ShieldType.ARTILLERY.getItem());
		inv.setItem(7, ShieldType.BACKSTAB.getItem());
		ownerPlayer.openInventory(inv);
	}

	public void openFacingInv() {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		Objects.requireNonNull(ownerPlayer);

		ItemStack isN = Banner.N.create(DyeColor.WHITE, DyeColor.BLACK,
				MessageHandler.getInstance().getString(ownerPlayer, "facingNorth"));
		// süden
		ItemStack isS = Banner.S.create(DyeColor.WHITE, DyeColor.BLACK,
				MessageHandler.getInstance().getString(ownerPlayer, "facingSouth"));
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_facingInv"));
		inv.setItem(2, isN);
		inv.setItem(6, isS);
		ownerPlayer.openInventory(inv);
	}

	public void openTierInv() {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		Objects.requireNonNull(ownerPlayer);

		String inventoryName = MessageHandler.getInstance().getString(ownerPlayer, "tbs_editor_tierInv");
		Inventory inv = Bukkit.createInventory(null, 9, inventoryName);
		inv.setItem(2, Banner.ONE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier I"));
		inv.setItem(4, Banner.TWO.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier II"));
		inv.setItem(6, Banner.THREE.create(DyeColor.WHITE, DyeColor.BLACK, "§rTier III/IV"));
		ownerPlayer.openInventory(inv);
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

	public void removeModule(ShieldPosition pos) {
		Set<ShieldModule> moduleClone = new HashSet<>();
		moduleClone.addAll(modules);
		try {
			modules.forEach(module -> {
				if (module.getPosition() == pos) {
					moduleClone.remove(module);
					return;
				}
			});
		} catch (ConcurrentModificationException e) {
		}
		modules = moduleClone;
	}

	public void setChoosedPosition(ShieldPosition choosedPosition) {
		this.choosedPosition = choosedPosition;
	}

	public void startSave() {
		/* First: Visualize */
		startVisualize();
		save = true;
		/* Then start normel TBS to save */

	}

	public void save(ChooseTestBlock chooseTB) {
		chooseTB.setType(Type.SHIELDS).setTestBlockType(TestBlockType.NEW);
		TestBlock slave = BauPlayer.getBauPlayer(owner).getTestBlockSlave();
		slave.setChooseTB(chooseTB);
		slave.showParticle();
		save = false;
	}

	public void startVisualize() {
		openFacingInv();

		/* Open Tier and Facing Inv */
		/* Then visualize all compnents */
		/* Saving into tbs undo the full region! */
	}

	public void visualize() {

		/* Save bigger region to UndoManager */
		Player ownerPlayer = Objects.requireNonNull(Bukkit.getPlayer(owner));

		TestBlock slave = BauPlayer.getBauPlayer(owner).getTestBlockSlave();
		Region rg = calcUndoRegion();
		slave.getUndoManager().addUndo(rg, CoordGetter.locToVec(ownerPlayer.getLocation()),
				BukkitAdapter.adapt(ownerPlayer.getWorld()));

		/* Paste classic TB (without saving!) */
		ChooseTestBlock tb = new ChooseTestBlock();
		tb.setTestBlockType(TestBlockType.DEFAULT);
		tb.setFacing(facing).setTier(tier).setType(Type.NORMAL);
		slave.pasteBlock(tb, false);
		if (save) {
			save(tb);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				/* visualizing single modules */
				Plot plot = BauPlayer.getBauPlayer(owner).getCurrentPlot();
				World world = ownerPlayer.getWorld();
				Iterator<ShieldModule> iter = modules.iterator();
				Scheduler scheduler = new Scheduler();

				scheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						if (iter.hasNext()) {
							iter.next().visualize(plot, world, tier, facing);

						} else {
							scheduler.cancel();
						}

					}
				}, 0, 4));

			}
		}, 30);

	}

	private Region calcUndoRegion() {
		Region normal = TestBlockCore.getTBRegion(tier, BauPlayer.getBauPlayer(owner).getCurrentPlot(), facing);
		int shieldSize = TestBlockCore.getMaxShieldSizeOfTier(tier);
		BlockVector3 min = BlockVector3.at(-shieldSize, 0, -shieldSize);
		BlockVector3 max = BlockVector3.at(shieldSize, shieldSize, shieldSize);
		try {
			normal.expand(min, max);
		} catch (RegionOperationException e) {
			e.printStackTrace();
		}
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

	public ShieldPosition getPos() {
		return choosedPosition;
	}
}
