package de.AS.Bau.Tools.TestBlockSlave;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.HikariCP.DataSource;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.CustomTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.EmptyTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.TestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.TestBlockType;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Type;
import de.AS.Bau.WorldEdit.UndoManager;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.ClickAction;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.JsonCreater;
import de.AS.Bau.utils.Scheduler;

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
		try (Connection conn = DataSource.getConnection()) {
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
		}
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
	
	/* CHOOSING*/
	
	public void startNewChoose() {
		chooseTB = new ChooseTestBlock();
	}
	
	public void startNewChoose(TestBlockType tbtype, int i) {
		chooseTB = new ChooseTestBlock();
		chooseTB.setTier(i);
		chooseTB.setTestBlockType(tbtype);
	}
	
	public void startNewChoose(ItemStack clicked) {
		chooseTB = new ChooseTestBlock();
		chooseTB.setTestBlock(getBlockOutOfBanner(clicked));
	}
	
	/* paste a testBlock */

	public void pasteBlock(TestBlock block, Facing facing) {
		String rgID = WorldGuardHandler.getPlotId(owner.getLocation());

		/* paste */

		WorldEditHandler.pasteTestBlock(block.getSchematic(), facing, rgID, owner);
		last = block;
		lastFacing = facing;

	}

	public void pasteBlock(String name, Facing facing) {
		pasteBlock(getBlockOutOfName(name), facing);
	}

	public void pasteBlock(ChooseTestBlock chooseTB2, Facing facing) {
		pasteBlock(chooseTB2.getTestBlock(), facing);
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

	public boolean addNewCustomTestBlock(int tier, String name, Facing facing, String plotID) {

		if (testblocks.get(tier).size() == 9) {
			Main.send(owner, "tbs_tooManyBlocks", "" + tier);
			return false;
		}
		if (nameExists(tier, name)) {
			Main.send(owner, "tbs_nameNotFree", "" + tier, name);
			return false;
		}
		saveRegionAsBlock(tier, facing, plotID, name);

		HashSet<CustomTestBlock> adding = testblocks.get(tier);
		CustomTestBlock block = new CustomTestBlock(owner, name, facing, tier);
		adding.add(block);
		testblocks.put(tier, adding);
		putTestBlockToDatabase(block);
		return true;
	}

	private void putTestBlockToDatabase(CustomTestBlock block) {
		try (Connection conn = DataSource.getConnection()) {
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
		}
	}

	private void saveRegionAsBlock(int tier, Facing facing, String plotID, String name) {
		BlockVector3 middle = CoordGetter.getMiddleRegionTB(plotID, facing);
		BlockVector3 sizes = CoordGetter.getMaxSizeOfBlock(tier);

		/* If North -> middle.z = min.z ! */

		BlockVector3 min;
		BlockVector3 max;
		if (facing == Facing.NORTH) {
			min = middle.subtract(sizes.divide(2).getX(), 0, 1);
			max = middle.add(sizes.divide(2).getX(), sizes.getY() - 1, sizes.getZ());
		} else {
			min = middle.subtract(sizes.divide(2).getX(), 0, sizes.getZ());
			max = middle.add(sizes.divide(2).getX(), sizes.getY() - 1, -1);
		}
		Region rg = new CuboidRegion(min, max);
		Clipboard board = WorldEditHandler.createClipboardOutOfRegion(rg,
				CoordGetter.getTBSPastePosition(plotID, facing), BukkitAdapter.adapt(owner.getWorld()));
		WorldEditHandler.saveClipboardAsSchematic(
				Main.schempath + "/" + owner.getUniqueId().toString() + "/TestBlockSklave", name + ".schem", board);

	}

	public boolean setTestBlockToFavorite(ItemStack itemStack) {
		if (readFavs().size() == 9) {
			Main.send(owner, "tbs_tooManyFavorites", "fa");
		}
		CustomTestBlock block = getBlockOutOfBanner(itemStack);
		block.setFavorite(true);
		updateFavToDataBase(true, block.getTier(), block.getName());
		Main.send(owner, "tbs_favAdded", block.getName());
		return true;
	}

	/* Removing TestBlocks */

	public boolean removeFavorite(ItemStack itemStack) {
		CustomTestBlock block = getBlockOutOfBanner(itemStack);
		if (!block.isFavorite()) {
			Main.send(owner, "tbs_blockIsNoFavorite");
			return false;
		}
		block.setFavorite(false);
		updateFavToDataBase(false, block.getTier(), block.getName());
		Main.send(owner, "tbs_favRemoved", block.getName());
		return true;

	}

	public boolean deleteTestBlock(int tier, String name) {
		HashSet<CustomTestBlock> blocks = testblocks.get(tier);
		if (!deleteTestBlockFromDatabase(tier, name)) {
			return false;
		}
		if (!blocks.remove(getBlockOutOfName(name))) {
			return false;
		}
		Main.send(owner, "tbs_tbDeleted", "" + tier, name);
		return true;
	}

	private boolean deleteTestBlockFromDatabase(int tier, String name) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("DELETE FROM TestBlock WHERE owner = ? AND tier = ? AND name = ?");
			statement.setString(1, owner.getUniqueId().toString());
			statement.setInt(2, tier);
			statement.setString(3, name);
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	private TestBlock getBlockOutOfName(String name) {
		for (Entry<Integer, HashSet<CustomTestBlock>> testBlockEntries : testblocks.entrySet()) {
			for (CustomTestBlock block : testBlockEntries.getValue()) {
				if (block.getName().equals(name)) {
					return block;
				}
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
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("UPDATE TestBlock SET favorite = ? WHERE owner = ? AND tier = ? AND name = ?");
			statement.setBoolean(1, fav);
			statement.setString(2, owner.getUniqueId().toString());
			statement.setInt(3, tier);
			statement.setString(4, name);
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	public void savingNewTBName(int tier) {
		saveTBParticles.cancel();
		/* Anvil Inv opening */
		TestBlockSlaveGUI.ChooseNameInv(owner, tier);
	}
	
	public void saveNewCustomTB(String name) {
		
		/* Save */
		addNewCustomTestBlock(newTBToSave.getTier(), name, newTBToSave.getfacing(),newTBToSave.getPlotID());
		/*Message to player*/
		Main.send(owner, "tbs_saveOwnTB_success",""+newTBToSave.getTier(),name);
		}

	public void showParticle() {
		JsonCreater creator = new JsonCreater(StringGetterBau.getString(owner, "tbs_gui_confirmRegion"));
		JsonCreater click = new JsonCreater(StringGetterBau.getString(owner, "tbs_gui_confirmRegionConfirm"));
		click.addHoverEvent(StringGetterBau.getString(owner, "tbs_gui_confirmRegionHover")).addClickEvent(
				"/tbs confirmRegion " + owner.getUniqueId(), ClickAction.RUN_COMMAND);
		creator.addJson(click).send(owner);

		String plotID = WorldGuardHandler.getPlotId(owner.getLocation());
		
		/* currentSelection: New_TB_TIER_FACING_TYPE */
		

		Facing facing = chooseTB.getFacing();
		int tier = chooseTB.getTier();
		BlockVector3 middle = CoordGetter.getMiddleRegionTB(plotID, facing);
		BlockVector3 sizes = CoordGetter.getMaxSizeOfBlock(tier);

		/* If North -> middle.z = min.z ! */

		BlockVector3 min;
		BlockVector3 max;

		if (facing == Facing.NORTH) {
			min = middle.subtract(sizes.divide(2).getX(), 0, -1);
			max = middle.add(sizes.divide(2).getX(), sizes.getY() - 1, sizes.getZ());
		} else {
			min = middle.subtract(sizes.divide(2).getX(), 0, sizes.getZ());
			max = middle.add(sizes.divide(2).getX(), sizes.getY() -1 , -1);
		}
		if (chooseTB.getType().equals(Type.SHIELDS)) {
			ConfigurationSection section = Main.getPlugin().getConfig()
					.getConfigurationSection("coordinates.tbs.sizes." + tier);
			int shieldSize = section.getInt("shields");
			min = min.subtract(shieldSize, 0, shieldSize);
			max = max.add(shieldSize, shieldSize, shieldSize);
		}
		BlockVector3 minVector = min;
		BlockVector3 maxVector = max;
		newTBToSave = new EmptyTestBlock(tier,new CuboidRegion(min, max),plotID,facing,owner.getWorld());
		saveTBParticles.cancel();
		saveTBParticles.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				TestBlockSlaveParticles.showTBParticlesShield(owner, minVector, maxVector);
			}
		}, 0,20));

	}

	

	

	

}
