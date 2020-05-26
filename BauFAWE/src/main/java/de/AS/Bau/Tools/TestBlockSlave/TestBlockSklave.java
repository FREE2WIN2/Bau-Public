package de.AS.Bau.Tools.TestBlockSlave;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.bukkit.entity.Player;

import de.AS.Bau.HikariCP.DataSource;

public class TestBlockSklave {

	/*
	 * this is the personal testBlockSlave for every Player it handles all personal
	 * testblocks and you can open the editor over him and save own testblocks.
	 * 
	 */

	Player owner;
	HashSet<TestBlock> tier1;
	HashSet<TestBlock> tier2;
	HashSet<TestBlock> tier3And4;
	HashSet<TestBlock> favorites;
	public TestBlockSklave(Player owner) {
		favorites = new HashSet<>();
		tier1 = readTestBlocks(1);
		tier2 = readTestBlocks(2);
		tier3And4 = readTestBlocks(3);
	}

	private HashSet<TestBlock> readTestBlocks(int tier) {
		HashSet<TestBlock> outSet = new HashSet<>();
		try {
			PreparedStatement statement = DataSource.getConnection().prepareStatement(
					"SELECT * FROM TestBlock,Player WHERE Player.UUID = ? AND TestBlock.tier = ? AND Player.UUID = TestBlock.owner");
			statement.setString(1, owner.toString());
			statement.setInt(1, tier);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				if(rs.getBoolean("favorite")) {
					favorites.add(new TestBlock(rs.getString("owner"), rs.getString("schemName"), rs.getString("name"), tier,
							rs.getBoolean("favorite")));
				}else {
					outSet.add(new TestBlock(rs.getString("owner"), rs.getString("schemName"), rs.getString("name"), tier,
							rs.getBoolean("favorite")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return outSet;
	}

	public void openGUI() {
		
	}
	
}
