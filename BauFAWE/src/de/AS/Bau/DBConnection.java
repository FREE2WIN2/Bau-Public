package de.AS.Bau;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.entity.Player;


public class DBConnection {
	private static Connection connection;

	public DBConnection() {
		Main main = Main.getPlugin();
		String database = main.getCustomConfig().getString("MySQL.database");
		String host = main.getCustomConfig().getString("MySQL.host");
		String user = main.getCustomConfig().getString("MySQL.user");
		String passwd = main.getCustomConfig().getString("MySQL.password");
		try {
			Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
			String connectionCommand = "jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password="
					+ passwd;
			connection = DriverManager.getConnection(connectionCommand);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("failed Connection");

		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	public ResultSet getMemberedPlots(Player p) {
		String uuid = p.getUniqueId().toString();
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "SELECT Plot_PlotID FROM `Player_has_Plot` WHERE `Player_UUID` = '" + uuid + "'";
			ResultSet rs = statement.executeQuery(sql);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean hasOwnPlots(Player p) {
		String uuid = p.getUniqueId().toString();
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "SELECT * FROM `Plot` WHERE `PlotID` = '" + uuid + "'";
			ResultSet rs = statement.executeQuery(sql);
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

	public boolean hasOwnPlots(String playerName) {
		String uuid = getUUID(playerName);
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "SELECT * FROM `Plot` WHERE `PlotID` = '" + uuid + "'";
			ResultSet rs = statement.executeQuery(sql);
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

	
	public boolean registerNewPlot(Player owner) {
		String uuid = owner.getUniqueId().toString();
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "INSERT INTO `Plot`(`PlotID`) VALUES ('" + uuid + "')";
			statement.execute(sql);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addMember(Player owner, String memberName) {
		String ownerUUID = owner.getUniqueId().toString();
		String memberUUID = getUUID(memberName);
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "INSERT INTO `Player_has_Plot`(`Plot_PlotID`, `Player_UUID`) VALUES ('" + ownerUUID + "','"
					+ memberUUID + "')";
			statement.execute(sql);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean removeMember(String uuidOwner, String memberName) {
		String memberUUID = getUUID(memberName);
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "DELETE FROM `Player_has_Plot` WHERE `Player_UUID` = '" + memberUUID + "' AND Plot_PlotID = '"
					+ uuidOwner + "'";
			statement.execute(sql);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isMember(Player p, String plotsPlayerName) {
		String memberUUID = p.getUniqueId().toString();
		String ownerUUID = getUUID(plotsPlayerName);
		if (memberUUID.equals(ownerUUID)) {
			return true;
		} else {
			try {
				Statement statement = DBConnection.connection.createStatement();
				String sql = "SELECT * FROM `Player_has_Plot` WHERE `Plot_PlotID` = '" + ownerUUID
						+ "' AND `Player_UUID` = '" + memberUUID + "'";
				ResultSet rs = statement.executeQuery(sql);
				return rs.next();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public boolean isMemberNames(String Membername, Player owner) {
		String memberUUID = getUUID(Membername);
		String ownerUUID = owner.getUniqueId().toString();
		if (memberUUID.equals(ownerUUID)) {
			return true;
		} else {
			try {
				Statement statement = DBConnection.connection.createStatement();
				String sql = "SELECT * FROM `Player_has_Plot` WHERE `Plot_PlotID` = '" + ownerUUID
						+ "' AND `Player_UUID` = '" + memberUUID + "'";
				ResultSet rs = statement.executeQuery(sql);
				return rs.next();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	public String getUUID(String name) {
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "SELECT `uuid` FROM `Player` WHERE `name` = '" + name + "'";
			ResultSet rs = statement.executeQuery(sql);
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
			Statement statement = DBConnection.connection.createStatement();
			String sql = "SELECT `name` FROM `Player` WHERE `UUID` = '" + UUID + "'";
			ResultSet rs = statement.executeQuery(sql);
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
		try {
			PreparedStatement statement = DBConnection.connection.prepareStatement("SELECT `countrycode` FROM `Player` WHERE `UUID`= ?");
			statement.setString(1, p.getUniqueId().toString());
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
	
	public String getLanguage(String uuid) {
		try {
			PreparedStatement statement = DBConnection.connection.prepareStatement("SELECT `countrycode` FROM `Player` WHERE `UUID`= ?");
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
			Statement statement = DBConnection.connection.createStatement();
			String sql = "SELECT Player_UUID FROM `Player_has_Plot` WHERE `Plot_PlotID` = '" + plotName + "'";
			ResultSet rs = statement.executeQuery(sql);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getRichtung() {
	return null;
	}

	public boolean deleteGs(String uuid) {
		try {
			Statement statement = DBConnection.connection.createStatement();
			String sql = "DELETE FROM `Player_has_Plot` WHERE `Plot_PlotID` = '"+uuid+"'";
			statement.execute(sql);
			String sqls = "DELETE FROM `Plot` WHERE `PlotID` = '" + uuid + "'";
			statement.execute(sqls);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	public boolean addMail(String sender, String uuidReciever, String message) {
		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO Mail(`PlayerUUID`,`sender`,`message`) VALUES(?,?,?)");
			statement.setString(1, uuidReciever);
			statement.setString(2, sender);
			statement.setString(3, message);
			return statement.executeUpdate()==1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	
public void closeConn() {
	try {
		DBConnection.connection.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}
}
}
