package net.wargearworld.Bau.Communication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

import org.bukkit.Bukkit;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.HikariCP.DataSource;



public class DatabaseCommunication {

	private static String nameOfMe = "Bau";
	public static int repeatDelay = 5*20; // 5Sekunden Delay
	
	public static void sendMessage(String reciever, String subchannel, String command) {
		try (Connection conn = DataSource.getConnection()) {
			String sql = "INSERT INTO `PluginCommunication`(`senderPlugin`, `recieverPlugin`, `subchannel`, `command`) VALUES (?,?,?,?)";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, nameOfMe);
			statement.setString(2, reciever);
			statement.setString(3, subchannel);
			statement.setString(4, command);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static Stack<PluginMessage> readMessages() {
		Stack<PluginMessage> out = new Stack<>();
		try (Connection conn = DataSource.getConnection()) {
			String sql = "SELECT * FROM `PluginCommunication` WHERE recieverPlugin = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, nameOfMe);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("messageID");
				String from = rs.getString("senderPlugin");
				String subChannel = rs.getString("subchannel");
				String command = rs.getString("command");
				out.add(new PluginMessage(id, from, subChannel, command));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return out;
	}

	public static void deleteMessage(int id) {
		try (Connection conn = DataSource.getConnection()) {
			String sql = "DELETE FROM PluginCommunication WHERE messageID = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, id);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void startRecieve() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				for (PluginMessage message : readMessages()) {
					message.operate();
				}
			}
		}, 0, repeatDelay);
	}

	public static void sendACK(String receiever, String subChannel, String command) {
		sendMessage(receiever, subChannel, command);	
	}
	
	
}
