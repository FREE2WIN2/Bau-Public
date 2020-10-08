package net.wargearworld.bau.tools.testBlockSlave;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.CharMatcher;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.session.ClipboardHolder;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.CustomTestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.EmptyTestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.TestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.TestBlockType;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Type;
import net.wargearworld.bau.worldedit.UndoManager;
import net.wargearworld.bau.worldedit.WorldEditHandler;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.CoordGetter;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.utils.Scheduler;

public class TestBlockSlave {

	/*
	 * this is the personal testBlockSlave for every Player it handles all personal
	 * testblocks and you can open the editor over him and save own testblocks.
	 * 
	 */
	private Player owner;
	private HashMap<Integer, HashSet<CustomTestBlock>> testblocks;
	private TestBlock last;
	private Facing lastFacing;
	private UndoManager undoManager;
	private Scheduler saveTBParticles;
	private EmptyTestBlock newTBToSave;
	private ChooseTestBlock chooseTB;
	/* init */

	public TestBlockSlave(Player owner) {
		this.owner = owner;
		testblocks = new HashMap<>();
		testblocks.put(1, readTestBlocks(1));
		testblocks.put(2, readTestBlocks(2));
		testblocks.put(3, readTestBlocks(3));
		last = null;
		undoManager = new UndoManager(owner);
		saveTBParticles = new Scheduler();
	}

	private HashSet<CustomTestBlock> readTestBlocks(int tier) {
		HashSet<CustomTestBlock> outSet = new HashSet<>();
		/*try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn.prepareStatement(
					"SELECT * FROM TestBlock,Player WHERE Player.UUID = ? AND TestBlock.tier = ? AND Player.UUID = TestBlock.owner");
			statement.setString(1, owner.getUniqueId().toString());
			statement.setInt(2, tier);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {

				outSet.add(new CustomTestBlock(rs.getString("owner"), rs.getString("schemName"), rs.getString("name"),
						rs.getString("richtung"), tier, rs.getBoolean("favorite")));

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		return outSet;
	}

	private HashSet<CustomTestBlock> readFavs() {
		HashSet<CustomTestBlock> favs = new HashSet<>();
		for (Entry<Integer, HashSet<CustomTestBlock>> tbs : testblocks.entrySet()) {
			for (CustomTestBlock block : tbs.getValue()) {
				if (block.isFavorite()) {
					favs.add(block);
				}
			}
		}
		return favs;
	}

	public void openGUI() {
		owner.openInventory(TestBlockSlaveGUI.tbsStartInv(owner, readFavs()));
	}

	/* Getter */

	public TestBlock getlastTestBlock() {
		return last;
	}

	public Facing getLastFacing() {
		return lastFacing;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public ChooseTestBlock getChooseTB() {
		return chooseTB;
	}

	/* CHOOSING */

	public void startNewChoose() {
		chooseTB = new ChooseTestBlock();
	}

	public void startNewChoose(TestBlockType tbtype, int i) {// Defaults
		chooseTB = new ChooseTestBlock();
		chooseTB.setTier(i);
		chooseTB.setTestBlockType(tbtype);
	}

	public void startNewChoose(ItemStack clicked) {// Customs
		chooseTB = new ChooseTestBlock();
		chooseTB.setTestBlock(getBlockOutOfBanner(clicked));
		chooseTB.setTestBlockType(TestBlockType.CUSTOM);
	}

	/* paste a testBlock */

	public void pasteBlock(TestBlock block, Facing facing, boolean saveUndo) {
		String rgID = WorldGuardHandler.getPlotId(owner.getLocation());
		/* paste */

		WorldEditHandler.pasteTestBlock(block.getSchematic(), facing, rgID, owner, saveUndo);
		last = block;
		lastFacing = facing;

	}

	public void pasteBlock(ChooseTestBlock chooseTB2, boolean saveUndo) {
		pasteBlock(chooseTB2.getTestBlock(), chooseTB2.getFacing(), saveUndo);
	}

	public void undo() {
		/* Undo last TB */

		Clipboard undo = undoManager.getUndo();
		if (undo == null) {
			Main.send(owner, "tbs_noUndo");
			return;
		}
		WorldEditHandler.pasteAsync(new ClipboardHolder(undo), undo.getOrigin(), owner, false, 1, false, true);
		Main.send(owner, "tbs_undo");
	}

	/* Adding TestBlocks */

	public boolean addNewCustomTestBlock(String name) {
		int tier = newTBToSave.getTier();
		String plotID = newTBToSave.getPlotID();
		Facing facing = newTBToSave.getfacing();
		Type type = newTBToSave.getType();
		if (testblocks.get(tier).size() == 9) {
			Main.send(owner, "tbs_tooManyBlocks", "" + tier);
			return false;
		}
		if (nameExists(tier, name)) {
			Main.send(owner, "tbs_nameNotFree", "" + tier, name);
			return false;
		}
		saveRegionAsBlock(tier, facing, plotID, name,type);
		HashSet<CustomTestBlock> adding = testblocks.get(tier);
		CustomTestBlock block = new CustomTestBlock(owner, name, facing, tier);
		adding.add(block);
		testblocks.put(tier, adding);
		putTestBlockToDatabase(block);
		return true;
	}

	private void putTestBlockToDatabase(CustomTestBlock block) {
		/*try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn.prepareStatement(
					"INSERT INTO `TestBlock`(`owner`, `schemName`, `name`, `tier`, `richtung`, `favorite`) VALUES (?,?,?,?,?,?)");
			statement.setString(1, owner.getUniqueId().toString());
			statement.setString(2, block.getName() + ".schem");
			statement.setString(3, block.getName());
			statement.setInt(4, block.getTier());
			statement.setString(5, block.getSchematic().getFacing().getFace());
			statement.setBoolean(6, block.isFavorite());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}

	private void saveRegionAsBlock(int tier, Facing facing, String plotID, String name,Type type) {
		Region rg = TestBlockSlaveCore.getTBRegion(tier, plotID, facing,owner.getWorld().getName());
		if(type.equals(Type.SHIELDS)) {
			int shielsSize = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
			try {
				rg.expand(BlockVector3.at(shielsSize, shielsSize, shielsSize),BlockVector3.at(-shielsSize, 0, -shielsSize));
			} catch (RegionOperationException e) {
			}
		}
		Clipboard board = WorldEditHandler.createClipboardOutOfRegion(rg,
				CoordGetter.getTBSPastePosition(plotID, facing, owner.getWorld().getName()), BukkitAdapter.adapt(owner.getWorld()));
		WorldEditHandler.saveClipboardAsSchematic(
				Main.schempath + "/" + owner.getUniqueId().toString() + "/TestBlockSklave", name + ".schem", board);

	}

	public boolean setTestBlockToFavorite(TestBlock tb) {
		if (readFavs().size() == 9) {
			Main.send(owner, "tbs_tooManyFavorites", "fa");
		}
		if (tb instanceof CustomTestBlock) {
			CustomTestBlock block = (CustomTestBlock) tb;
			block.setFavorite(true);
			updateFavToDataBase(true, block.getTier(), block.getName());
			Main.send(owner, "tbs_favAdded", block.getName());
			return true;
		}
		return false;
	}

	public void setTestBlockToFavorite(ItemStack clicked) {
		setTestBlockToFavorite(getBlockOutOfBanner(clicked));

	}

	/* Removing TestBlocks */

	public boolean removeFavorite(TestBlock tb) {
		if (tb instanceof CustomTestBlock) {
			CustomTestBlock block = (CustomTestBlock) tb;
			if (!block.isFavorite()) {
				Main.send(owner, "tbs_blockIsNoFavorite");
				return false;
			}
			block.setFavorite(false);
			updateFavToDataBase(false, block.getTier(), block.getName());
			Main.send(owner, "tbs_favRemoved", block.getName());
			return true;
		}
		return false;

	}

	public boolean deleteTestBlock(int tier, String name) {
		HashSet<CustomTestBlock> blocks = testblocks.get(tier);
		if (!deleteTestBlockFromDatabase(tier, name)) {
			return false;
		}
		TestBlock tb = getBlockOutOfName(name, tier);
		if (!blocks.remove(tb)) {
			return false;
		}
		tb.getSchematic().getFile().delete();
		Main.send(owner, "tbs_tbDeleted", "" + tier, name);
		return true;
	}

	private boolean deleteTestBlockFromDatabase(int tier, String name) {
		/*try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("DELETE FROM TestBlock WHERE owner = ? AND tier = ? AND name = ?");
			statement.setString(1, owner.getUniqueId().toString());
			statement.setInt(2, tier);
			statement.setString(3, name);
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		return false;
	}

	/* Helper Methods */

	public CustomTestBlock getBlockOutOfBanner(ItemStack itemStack) {
		for (Entry<Integer, HashSet<CustomTestBlock>> testBlockEntries : testblocks.entrySet()) {
			for (CustomTestBlock block : testBlockEntries.getValue()) {
				if (block.getBanner().equals(itemStack)) {
					return block;
				}
			}
		}
		return null;
	}

	private TestBlock getBlockOutOfName(String name, int tier) {
		for (CustomTestBlock block : testblocks.get(tier)) {
			if (block.getName().equals(name)) {
				return block;
			}
		}
		return null;
	}

	private boolean nameExists(int tier, String name) {
		for (CustomTestBlock block : testblocks.get(tier)) {
			if (block.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean updateFavToDataBase(boolean fav, int tier, String name) {
		/*try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("UPDATE TestBlock SET favorite = ? WHERE owner = ? AND tier = ? AND name = ?");
			statement.setBoolean(1, fav);
			statement.setString(2, owner.getUniqueId().toString());
			statement.setInt(3, tier);
			statement.setString(4, name);
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		return false;
	}
	/* Show ManageMent Inventory */

	public void showTBManager() {
		owner.openInventory(TestBlockSlaveGUI.tbManager(testblocks, owner));
	}

	public void openAddFavoriteInv() {
		owner.openInventory(TestBlockSlaveGUI.showAllNonFavorites(testblocks, owner));
	}

	public void startSavingNewTB() {
		startNewChoose();
		chooseTB.setTestBlockType(TestBlockType.NEW);
		owner.openInventory(TestBlockSlaveGUI.tierInv(owner));

	}

	public void savingNewTBName() {
		saveTBParticles.cancel();
		/* Anvil Inv opening */
		TestBlockSlaveGUI.ChooseNameInv(owner, chooseTB.getTier());
	}

	public void saveNewCustomTB(String name) {

		/* Save */
		name = name.replaceFirst(" ","");
		name = name.replace(" ", "_");
		boolean isValid = checkIfValid(name);
		if(!isValid) {
			Main.send(owner, "tbs_saveOwnTB_invalidName");
			Main.send(owner, "tbs_saveOwnTB_invalidLetters");
			return;
		}
		if (addNewCustomTestBlock(name)) {
			/* Message to player */
			Main.send(owner, "tbs_saveOwnTB_success", "" + newTBToSave.getTier(), name);
		}
	}

	private boolean checkIfValid(String name) {
		if(name.contains(".")) {
			return false;
		}
		if(name.startsWith(" ")&&name.length() == 1 || name.equals("")) {
			return false;
		}
		return CharMatcher.ascii().matchesAllOf(name);
	}

	public void showParticle() {
		JsonCreater creator = new JsonCreater(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegion"));
		JsonCreater click = new JsonCreater(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionConfirm"));
		click.addHoverEvent(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionHover"))
				.addClickEvent("/tbs confirmRegion " + owner.getUniqueId(), ClickAction.RUN_COMMAND);
		JsonCreater cancel = new JsonCreater(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionCancel"));
		cancel.addHoverEvent(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionCancelHover"))
				.addClickEvent("/tbs confirmRegionCancel " + owner.getUniqueId(), ClickAction.RUN_COMMAND);

		creator.addJson(click).addJson(cancel).send(owner);

		String plotID = WorldGuardHandler.getPlotId(owner.getLocation());

		/* currentSelection: New_TB_TIER_FACING_TYPE */

		Facing facing = chooseTB.getFacing();
		int tier = chooseTB.getTier();
		Region rg = TestBlockSlaveCore.getTBRegion(tier, plotID, facing,owner.getWorld().getName());
		BlockVector3 min = rg.getMinimumPoint();
		BlockVector3 max = rg.getMaximumPoint();
		if (chooseTB.getType().equals(Type.SHIELDS)) {
			int shieldSize = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
			min = min.subtract(shieldSize, 0, shieldSize);
			max = max.add(shieldSize, shieldSize, shieldSize);
		}
		BlockVector3 minVector = min;
		BlockVector3 maxVector = max;
		newTBToSave = new EmptyTestBlock(tier, new CuboidRegion(min, max), plotID, facing, owner.getWorld(),chooseTB.getType());
		saveTBParticles.cancel();
		saveTBParticles.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				TestBlockSlaveParticles.showTBParticlesShield(owner, minVector, maxVector);
			}
		}, 0, 20));

	}

	public void setChooseTB(ChooseTestBlock chooseTB) {
		this.chooseTB = chooseTB;
	}

	public void cancelSave() {
		if (saveTBParticles.isRunning()) {
			saveTBParticles.cancel();
			Main.send(owner, "tbs_save_canceled");
		} else {
			Main.send(owner, "tbs_save_NoSaveToCancel");
		}
	}
}
