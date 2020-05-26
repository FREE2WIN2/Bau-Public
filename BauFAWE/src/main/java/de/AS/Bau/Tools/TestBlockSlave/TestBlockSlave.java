package de.AS.Bau.Tools.TestBlockSlave;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.HikariCP.DataSource;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.CustomTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.TestBlock;
import de.AS.Bau.WorldEdit.UndoManager;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.Facing;

public class TestBlockSlave {

	/*
	 * this is the personal testBlockSlave for every Player it handles all personal
	 * testblocks and you can open the editor over him and save own testblocks.
	 * 
	 */

	private Player owner;
	private HashSet<CustomTestBlock> tier1;
	private HashSet<CustomTestBlock> tier2;
	private HashSet<CustomTestBlock> tier3And4;
	private HashSet<CustomTestBlock> favorites;
	private TestBlock last;
	private Facing lastFacing;
	private UndoManager undoManager;

	public TestBlockSlave(Player owner) {
		favorites = new HashSet<>();
		tier1 = readTestBlocks(1);
		tier2 = readTestBlocks(2);
		tier3And4 = readTestBlocks(3);
		last = null;
		undoManager = new UndoManager(owner);
	}

	private HashSet<CustomTestBlock> readTestBlocks(int tier) {
		HashSet<CustomTestBlock> outSet = new HashSet<>();
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn.prepareStatement(
					"SELECT * FROM TestBlock,Player WHERE Player.UUID = ? AND TestBlock.tier = ? AND Player.UUID = TestBlock.owner");
			statement.setString(1, owner.toString());
			statement.setInt(1, tier);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				if (rs.getBoolean("favorite")) {
					favorites.add(new CustomTestBlock(rs.getString("owner"), rs.getString("schemName"),
							rs.getString("name"),rs.getString("richtung"), tier, rs.getBoolean("favorite")));
				} else {
					outSet.add(new CustomTestBlock(rs.getString("owner"), rs.getString("schemName"),
							rs.getString("name"),rs.getString("richtung"), tier, rs.getBoolean("favorite")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return outSet;
	}

	public void openGUI() {
		owner.openInventory(TestBlockSlaveGUI.tbsStartInv(owner, favorites));
	}

	public TestBlock getlastTestBlock() {
		return last;
	}
	public Facing getLastFacing() {
		return lastFacing;
	}

	public void pasteBlock(TestBlock block, Facing facing) {

		if (last == null) {
			owner.sendMessage(Main.prefix + StringGetterBau.getString(owner, "noLastPaste"));
			return;
		}
		String rgID = WorldGuardHandler.getPlotId(owner.getLocation());

		/* paste */

		WorldEditHandler.pasteTestBlock(block.getSchematic(), facing, rgID, owner);
		last = block;
		lastFacing = facing;


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
}
