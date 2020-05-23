package de.AS.Bau.cmds;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.GUI;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.WorldHandler;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;

public class gs implements CommandExecutor {

	public static String joinPLot = Main.getPlugin().getCustomConfig().getString("coordinates.spawn");
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

		DBConnection conn = new DBConnection();
		Player p = (Player) sender;
		if (args.length == 0) {
			// gs -> tp zum own gs
			p.teleport(CoordGetter.getTeleportLocation(WorldHandler.loadWorld(p.getUniqueId().toString()), joinPLot));
			conn.closeConn();
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				// gs list
				ArrayList<String> memberedPlots = new ArrayList<>();
				memberedPlots.add(p.getName());
				ResultSet rs = conn.getMemberedPlots(p);
				try {
					while (rs.next()) {
						memberedPlots.add(conn.getName(rs.getString(1)));
					}
					sendMemberedGs(p, memberedPlots, conn);
				} catch (SQLException e) {
					e.printStackTrace();
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
				}
				conn.closeConn();
				return true;

			}
			if (args[0].equalsIgnoreCase("info")) {
				// bau info
				// open gui
				GUI.showMember(p);
				Main.send(p, "timeShow", p.getWorld().getTime()+"");
				conn.closeConn();
				return true;

			}
		} else if (args.length == 2) {
			switch (args[0].toLowerCase()) {
			case "time":
				setTime(p, Integer.parseInt(args[1]));
				break;
			case "addtemp":
				p.sendMessage(Main.prefix
						+ StringGetterBau.getString(p, "wrongCommand").replace("%r", "gs addtemp <Spieler> <Zeit(h)>"));
				break;
			case "tempadd":
				p.sendMessage(Main.prefix
						+ StringGetterBau.getString(p, "wrongCommand").replace("%r", "gs tempadd <Spieler> <Zeit(h)>"));
				break;
			case "remove":
				removeMember(p, args[1], conn);
				// bau remove [PlayerName]
				break;
			case "add":

				// Befehle: /bau add [PlayerName]
				break;
			case "tp":
				// if (conn.isMember(p, args[1]) || p.hasPermission("moderator")) {
				if ((conn.isMember(p, args[1]) || p.hasPermission("moderator")) && conn.hasOwnPlots(args[1])) {
					String plotID = conn.getUUID(args[1]);
					p.teleport(CoordGetter.getTeleportLocation(WorldHandler.loadWorld(plotID),joinPLot));
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noPlotMember"));
				}
				// bau tp [Playername]
				break;
			case "delete":
				if (p.hasPermission("admin")) {

				} else {
					conn.closeConn();
					return false;
				}
				break;
			}
			conn.closeConn();
			return true;

		} else if (args.length == 3) {
			switch (args[0].toLowerCase()) {
			case "time":
			case "zeit":
				if (args[1].equalsIgnoreCase("set")) {
					setTime(p, Integer.parseInt(args[2]));
					return true;
				} else {
					Main.send(p, "wrongCommand", "gs time set <time>");
					return true;
				}
			case "add":
			case "addtemp":
			case "tempadd":
				addMemberTemp(p, args[1], Integer.parseInt(args[2]), conn);
			}
			conn.closeConn();
			return true;
		} else {
			conn.closeConn();
			return false;
		}
		conn.closeConn();
		return false;
	}

	private void setTime(Player p, int time) {
		if (time > 24000) {
			Main.send(p, "timeTooHigh", "" + time);
		} else {
			p.getWorld().setTime(time);
			Main.send(p, "time", "" + time);
		}

	}

	public void removeMember(Player p, String playerNameToRemove, DBConnection conn) {
		if (!p.getName().equalsIgnoreCase(playerNameToRemove)) {
			if (conn.removeMember(p.getUniqueId().toString(), playerNameToRemove)) {
				p.sendMessage(Main.prefix
						+ StringGetterBau.getString(p, "plotMemberRemoved").replace("%r", playerNameToRemove));
				WorldGuardHandler.removeMemberFromAllRegions(p.getUniqueId().toString(),
						conn.getUUID(playerNameToRemove), playerNameToRemove);

			} else {
				Main.send(p, "error");
			}
		} else {
			Main.send(p, "YouCantRemoveYourself");
		}
	}

	public void addMember(Player p, String playerName, DBConnection conn) {
		if (conn.isMemberNames(playerName, p)) {
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "alreadyMember").replace("%r", playerName));
		} else {
			if (conn.addMember(p, playerName)) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "plotMemberAdded").replace("%r", playerName));
				WorldGuardHandler.addPlayerToAllRegions(p.getUniqueId().toString(), conn.getUUID(playerName));
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
			}
		}
	}

	public void addMemberTemp(Player p, String playerName, int time, DBConnection conn) {
		if (conn.isMemberNames(playerName, p)) {
			Main.send(p, "alreadyMember", playerName);
		} else {
			if (conn.addMember(p, playerName)) {
				String uuidMember = conn.getUUID(playerName);
				Main.send(p, "memberTempAdded", playerName, "" + time);
				WorldGuardHandler.addPlayerToAllRegions(p.getUniqueId().toString(), uuidMember);

				FileConfiguration config = Main.getPlugin().getTempAddConfig();
				Long Time;
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
				calendar.add(Calendar.HOUR_OF_DAY, time);
				Date date = calendar.getTime();
				Time = Long.parseLong(formatter.format(date));
				config.set(p.getUniqueId().toString() + "." + uuidMember, Time);
				try {
					config.save(Main.getPlugin().getTempAddConfigFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
			}
		}
	}

	public void sendMemberedGs(Player p, ArrayList<String> memberedPlots, DBConnection conn) {
		PlayerConnection pConn = ((CraftPlayer) p).getHandle().playerConnection;
		p.sendMessage(StringGetterBau.getString(p, "listGsHeading"));
		for (String s : memberedPlots) {
			String hover = StringGetterBau.getString(p, "listGsHover").replace("%r", s);
			String name = s;
			String txt = "{\"text\":\"§7[§6" + name
					+ "§7]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/gs tp " + name
					+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + hover + "\"}}}";
			IChatBaseComponent txtc = ChatSerializer.a(txt);
			PacketPlayOutChat txtp = new PacketPlayOutChat(txtc);
			pConn.sendPacket(txtp);
		}
		p.sendMessage("§7----------------------------");
		conn.closeConn();
	}

	public void deletePlot(Player p, String playerName, DBConnection conn) {
		String worldName = conn.getUUID(playerName);
		World w = WorldHandler.loadWorld(worldName);
		for (Player a : w.getPlayers()) {
			a.kickPlayer("GS DELETE");
		}
		if (WorldHandler.deleteWorld(w, conn)) {
			Main.send(p, "gsDeleted", playerName);
		} else {
			Main.send(p, "error");
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
		}
	}

	public void makeNewPlot() {
		
	}
	
	public static void startCheckForTempAdd() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				FileConfiguration config = Main.getPlugin().getTempAddConfig();
				Long Time;
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
				Date date = calendar.getTime();
				Time = Long.parseLong(formatter.format(date));
				for (String s : config.getKeys(false)) {
					String uuidOwner = s;
					for (String m : config.getConfigurationSection(s).getKeys(false)) {
						Long time = config.getLong(s + "." + m);
						if (time < Time) {
							String uuidMember = m;
							DBConnection conn = new DBConnection();
							if (conn.removeMember(uuidOwner, conn.getName(uuidMember))) {
								// wg remove member
								WorldGuardHandler.removeMemberFromAllRegions(uuidOwner, uuidMember, null);
							} else {
								System.err.println("Member konnte nicht entfernt werden : MeberUUID: " + uuidMember
										+ " | OwnerUUID: ");
							}
							// config remove
							config.set(uuidOwner + "." + uuidMember, null);
							try {
								config.save(Main.getPlugin().getTempAddConfigFile());
							} catch (IOException e) {
								e.printStackTrace();
							}

							/* Message to Owner */
							boolean ownerOn = false;
							String memberName = conn.getName(uuidMember);
							for (Player owner : Bukkit.getServer().getOnlinePlayers()) {
								if (owner.getUniqueId().toString().equals(uuidOwner)) {
									owner.sendMessage(Main.prefix + StringGetterBau
											.getString(owner, "plotMemberRemoved").replace("%r", memberName));
									ownerOn = true;
									break;
								}
							}
							if (!ownerOn) {
								conn.addMail("plugin: BAU", uuidOwner, StringGetterBau
										.getString(uuidOwner, "plotMemberRemoved").replace("%r", memberName));
							}

							// output?
							conn.closeConn();
						}

					}
				}
			}
		}, 0, 5 * 60 * 20);// 30*60*

	}

}
