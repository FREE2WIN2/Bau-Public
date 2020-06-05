package de.AS.Bau.HikariCP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.AS.Bau.Main;

public class DataSource {

	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	public DataSource() {
		Main main = Main.getPlugin();
		String database = main.getCustomConfig().getString("MySQL.database");
		String host = main.getCustomConfig().getString("MySQL.host");
		String user = main.getCustomConfig().getString("MySQL.user");
		String passwd = main.getCustomConfig().getString("MySQL.password");
		String connectionCommand = "jdbc:mysql://" + host + "/" + database;
		config.setJdbcUrl(connectionCommand);
		config.setUsername(user);
		config.setPassword(passwd);
		config.setSchema(database);
//		config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.setConnectionTimeout(570000);
		ds = new HikariDataSource(config);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	public ResultSet getMemberedPlots(Player p) {
		String uuid = p.getUniqueId().toString();
		try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT Plot_PlotID FROM `Player_has_Plot` WHERE `Player_UUID` = ?");
			statement.setString(1, uuid);
			ResultSet rs = statement.executeQuery();
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
