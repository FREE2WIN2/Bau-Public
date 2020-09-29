package net.wargearworld.Bau.HikariCP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.World.WorldTemplate;

public class DBConnection {
	public DBConnection() {
	}

	/**
	 * @param uuidPlayer -> The UUID of the Player who wants to know his plots he can join
	 * @return Set of the PlayerName he is added on.
	 */
	public static Set<String> getMemberedPlots(UUID uuidPlayer) {
		String uuid = uuidPlayer.toString();
		HashSet<String> out = new HashSet<>();
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT Player.name FROM Player_has_Plot,Player WHERE Player_has_Plot.Plot_PlotID = Player.UUID AND Player_has_Plot.Player_UUID = ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				out.add(rs.getString(1));
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean hasOwnPlots(String playerName) {
		String uuid = getUUID(playerName);
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT * FROM `Plot` WHERE `owner` = ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			boolean out = rs.next();
			System.out.println("hasPlot: " + out);
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	public static boolean addMember(UUID owner, String memberName) {
		
		UUID memberUUID = UUID.fromString(getUUID(memberName));
		return addMember(owner, memberUUID);
	}

	public static boolean addMember(UUID owner, UUID memberUUID) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("INSERT INTO `Player_has_Plot`(`Plot_PlotID`, `Player_UUID`) VALUES (?,?)");
			statement.setString(1, owner.toString());
			statement.setString(2, memberUUID.toString());
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean removeMember(int id, UUID member) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("DELETE FROM `Player_has_Plot` WHERE `Player_UUID` = ? AND Plot_PlotID = ?");
			statement.setString(1, member.toString());
			statement.setInt(2, id);
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean isMember(UUID member, String ownerName) {
		String memberUUID = member.toString();
		String ownerUUID = getUUID(ownerName);
		if (memberUUID.equals(ownerUUID)) {
			return true;
		} else {
			try (Connection conn = DataSource.getConnection()) {
				PreparedStatement statement = conn.prepareStatement(
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

	public static String getUUID(String name) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT `uuid` FROM `Player` WHERE `name` = ?");
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

	public static String getName(String UUID) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT `name` FROM `Player` WHERE `UUID` = ?");
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

	public static String getLanguage(Player p) {
		return getLanguage(p.getUniqueId().toString());
	}

	public static String getLanguage(String uuid) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT `countrycode` FROM `Player` WHERE `UUID`= ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return "de";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "de";
		}
	}

	public static Set<String> getMember(String plotName) {
		HashSet<String> out = new HashSet<>();
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT Player_UUID FROM `Player_has_Plot`,Player WHERE `Plot_PlotID` = ?");
			statement.setString(1, plotName);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				out.add(rs.getString(1));
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean deleteGs(String uuid) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("DELETE FROM `Player_has_Plot` WHERE `Plot_PlotID` = ?");
			statement.setString(1, uuid);
			if (!(statement.executeUpdate() >= 0)) {
				return false;
			}
			PreparedStatement statement2 = DataSource.getConnection()
					.prepareStatement("DELETE FROM `Plot` WHERE `PlotID` = ?");
			statement2.setString(1, uuid);
			return statement2.executeUpdate() == 1;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static boolean addMail(String sender, String uuidReciever, String message) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
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

	public static Set<String> getAllWorlds() {
		SortedSet<String> out = new TreeSet<>();
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
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

	
	public static String getTemplate(UUID ownerUUID) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT template FROM Plot WHERE PlotID = ?");
			statement.setString(1, ownerUUID.toString());
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getTemplate(int id) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT template FROM Plot WHERE ID = ?");
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Map<UUID, Date> getMembers(int id) {
		Map<UUID,Date> out = new HashMap<>();
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT UUID,addedTo FROM Player,Player_has_Plot WHERE Plot_PlotID = ? AND Player_UUID = Player.UUID");
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				out.put(UUID.fromString(rs.getString(1)),rs.getDate(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static boolean addMember(int id, UUID memberUUID, Date to) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("INSERT INTO `Player_has_Plot`(`Plot_PlotID`, `Player_UUID`, addedTo) VALUES (?,?,?)");
			statement.setInt(1, id);
			statement.setString(2, memberUUID.toString());
			if(to == null) {
				statement.setNull(3, Types.DATE);
			}else {
				statement.setDate(3, new java.sql.Date(to.getTime()));
			}
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean setTemplate(int id, WorldTemplate template) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("UPDATE `Plot` SET template = ? WHERE ID = ?");
			statement.setString(1, template.getName());
			statement.setInt(2, id);
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean registerNewPlot(String worldName, String ownerUUID) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("INSERT INTO `Plot`(name,owner,template) VALUES (?,?,?)");
			statement.setString(1, worldName);
			statement.setString(2, ownerUUID);
			statement.setString(3, WorldManager.template.getName());
			return statement.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int getID(String worldName, String owner) {
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT ID FROM `Plot` WHERE owner = ? AND name = ?");
			statement.setString(1, owner);
			statement.setString(2, worldName);
			ResultSet rs = statement.executeQuery();
			if(rs.next())
				return rs.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static Map<UUID,String> getAllNotAddedPlayers(int worldID) {
		Map<UUID,String> out = new HashMap<>();
		try (Connection conn = DataSource.getConnection()) {
			PreparedStatement statement = conn
					.prepareStatement("SELECT name,UUID FROM `Player` LEFT JOIN Player_has_Plot ON UUID = Player_UUID AND Plot_PlotID = ? WHERE Player_UUID IS NULL");
			statement.setInt(1, worldID);
			System.out.println(statement);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				out.put(UUID.fromString(rs.getString(2)), rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
}
