package net.wargearworld.Bau.HikariCP;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.wargearworld.Bau.Main;

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
		config.setConnectionTimeout(500000);
		config.setMaxLifetime(500000);
		config.setMaximumPoolSize(5);
		ds = new HikariDataSource(config);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

}
