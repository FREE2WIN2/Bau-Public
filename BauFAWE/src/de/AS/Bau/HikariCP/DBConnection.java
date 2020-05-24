package de.AS.Bau.HikariCP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.entity.Player;

public class DBConnection {
	public DBConnection() {
	}

	public ResultSet getMemberedPlots(UUID uuidPlayer) {
		String uuid = uuidPlayer.toString();
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT Plot_PlotID FROM `Player_has_Plot` WHERE `Player_UUID` = ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean hasOwnPlots(String playerName) {
		String uuid = getUUID(playerName);
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT * FROM `Plot` WHERE `PlotID` = ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean registerNewPlot(UUID owner) {
		String uuid = owner.toString();
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("INSERT INTO `Plot`(`PlotID`) VALUES (?)");
			statement.setString(1, uuid);
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addMember(UUID owner, String memberName) {
		String ownerUUID = owner.toString();
		String memberUUID = getUUID(memberName);
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("INSERT INTO `Player_has_Plot`(`Plot_PlotID`, `Player_UUID`) VALUES (?,?)");
			statement.setString(1, ownerUUID);
			statement.setString(2, memberUUID);
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean removeMember(String uuidOwner, String memberName) {
		String memberUUID = getUUID(memberName);
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("DELETE FROM `Player_has_Plot` WHERE `Player_UUID` = ? AND Plot_PlotID = ?");
			statement.setString(1, memberUUID);
			statement.setString(2, uuidOwner);
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isMember(UUID member, String ownerName) {
		String memberUUID = member.toString();
		String ownerUUID = getUUID(ownerName);
		if (memberUUID.equals(ownerUUID)) {
			return true;
		} else {
			try {
				PreparedStatement statement = DataSource.getConnection().prepareStatement(
						"SELECT * FROM `Player_has_Plot` WHERE `Plot_PlotID` = ? AND `Player_UUID` = ?");
				statement.setString(1, ownerUUID);
				statement.setString(2, memberUUID);
				ResultSet rs = statement.executeQuery();
				return rs.next();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public String getUUID(String name) {
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT `uuid` FROM `Player` WHERE `name` = ?");
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getName(String UUID) {
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT `name` FROM `Player` WHERE `UUID` = ?");
			statement.setString(1, UUID);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getLanguage(Player p) {
		return getLanguage(p.getUniqueId().toString());
	}

	public String getLanguage(String uuid) {
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT `countrycode` FROM `Player` WHERE `UUID`= ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return "de";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getMember(String plotName) {
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT Player_UUID FROM `Player_has_Plot` WHERE `Plot_PlotID` = ?");
			statement.setString(1, plotName);
			ResultSet rs = statement.executeQuery();
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean deleteGs(String uuid) {
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("DELETE FROM `Player_has_Plot` WHERE `Plot_PlotID` = ?");
			statement.setString(1, uuid);
			if (!(statement.executeUpdate() >= 0)) {
				return false;
			}
			PreparedStatement statement2 = DataSource.getConnection()
					.prepareStatement("DELETE FROM `Plot` WHERE `PlotID` = ?");

			return statement2.executeUpdate() == 1;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean addMail(String sender, String uuidReciever, String message) {
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("INSERT INTO Mail(`PlayerUUID`,`sender`,`message`) VALUES(?,?,?)");
			statement.setString(1, uuidReciever);
			statement.setString(2, sender);
			statement.setString(3, message);
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public Set<String> getAllWorlds() {
		SortedSet<String> out = new TreeSet<>();
		try {
			PreparedStatement statement = DataSource.getConnection()
					.prepareStatement("SELECT Player.name FROM Player,Plot WHERE Plot.PlotID = Player.UUID");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				out.add(rs.getString(1));
			}
			return out;
		} catch (SQLException e) {
			e.printStackTrace();
			return out;
		}
	}
}
