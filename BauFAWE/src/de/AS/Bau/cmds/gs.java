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
import de.AS.Bau.Listener.OnInvClick;
import de.AS.Bau.utils.WorldHandler;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;

public class gs implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

		DBConnection conn = new DBConnection();
		Player p = (Player) sender;
		if (args.length == 0) {
			// gs -> tp zum own gs
			if (!Bukkit.getServer().getWorlds().contains(Bukkit.getServer().getWorld(p.getUniqueId().toString()))) {
				WorldHandler.loadWorld(p.getUniqueId().toString());
			}
			Location loc = new Location(Bukkit.getServer().getWorld(p.getUniqueId().toString()), -208, 8, 17);
			p.teleport(loc);
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
				OnInvClick.showMember(p);
				p.sendMessage(StringGetterBau.getString(p, "timeShow") + " §7" + p.getWorld().getTime());
				conn.closeConn();
				return true;

			}
		} else if (args.length == 2) {
			switch (args[0].toLowerCase()) {
			case "time":
				int time = Integer.parseInt(args[1]);
				if (time > 24000) {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "timeTooHigh").replace("%r", args[1]));
				} else {
					p.getWorld().setTime(time);
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "time").replace("%r", args[1]));
				}
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
				if (!p.getName().equalsIgnoreCase(args[1])) {
					if (conn.removeMember(p.getUniqueId().toString(), args[1])) {
						p.sendMessage(
								Main.prefix + StringGetterBau.getString(p, "plotMemberRemoved").replace("%r", args[1]));
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionManager regions = container
								.get(BukkitAdapter.adapt(Bukkit.getWorld(p.getUniqueId().toString())));
						for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
							DefaultDomain member = rg.getValue().getMembers();
							member.removePlayer(args[1]);
							member.removePlayer(UUID.fromString(conn.getUUID(args[1])));
							rg.getValue().setMembers(member);
						}

					} else {
						p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
					}
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "YouCantRemoveYourself"));
				}
				// bau remove [PlayerName]
				break;
			case "add":
				if (conn.isMemberNames(args[1], p)) {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "alreadyMember").replace("%r", args[1]));
				} else {
					if (conn.addMember(p, args[1])) {
						p.sendMessage(
								Main.prefix + StringGetterBau.getString(p, "plotMemberAdded").replace("%r", args[1]));
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionManager regions = container
								.get(BukkitAdapter.adapt(Bukkit.getWorld(p.getUniqueId().toString())));

						for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
							DefaultDomain member = rg.getValue().getMembers();
							member.addPlayer(UUID.fromString(conn.getUUID(args[1])));
							rg.getValue().setMembers(member);
						}
					} else {
						p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
					}
				}
				// Befehle: /bau add [PlayerName]
				break;
			case "tp":
				// if (conn.isMember(p, args[1]) || p.hasPermission("moderator")) {
				if ((conn.isMember(p, args[1]) || p.hasPermission("moderator")) && conn.hasOwnPlots(args[1])) {
					String plotID = conn.getUUID(args[1]);
					if (!Bukkit.getServer().getWorlds().contains(Bukkit.getServer().getWorld(plotID))) {
						WorldHandler.loadWorld(plotID);
					}
					Location loc = new Location(Bukkit.getServer().getWorld(plotID), -208, 8, 17);
					p.teleport(loc);
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noPlotMember"));
				}
				// bau tp [Playername]
				break;
			case "delete":
				if (p.hasPermission("admin")) {
					String worldName = conn.getUUID(args[1]);

					World w;
					if (!Bukkit.getServer().getWorlds().contains(Bukkit.getServer().getWorld(worldName))) {
						w = WorldHandler.loadWorld(worldName);
					} else {
						w = Bukkit.getServer().getWorld(worldName);
					}
					if (!w.getPlayers().isEmpty()) {
						for (Player a : w.getPlayers()) {
							a.kickPlayer("GS DELETE");
						}
					}

					if (WorldHandler.deleteWorld(w, conn)) {
						p.sendMessage(Main.prefix + StringGetterBau.getString(p, "gsDeleted").replace("%r", args[1]));
					} else {
						p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
					}

				} else {
					conn.closeConn();
					return false;
				}
				break;
			}
			conn.closeConn();
			return true;

		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("time") && args[1].equalsIgnoreCase("set")) {
				int time = Integer.parseInt(args[1]);
				if (time > 24000) {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "timeTooHigh").replace("%r", args[1]));
				} else {
					p.getWorld().setTime(time);
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "time").replace("%r", args[1]));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("time")) {
				p.sendMessage(
						Main.prefix + StringGetterBau.getString(p, "wrongCommand").replace("%r", "gs time <time>"));
				return true;
			}

			switch (args[0].toLowerCase()) {

			case "add":
			case "addtemp":
			case "tempadd":
				if (conn.isMemberNames(args[1], p)) {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "alreadyMember").replace("%r", args[1]));
				} else {
					if (conn.addMember(p, args[1])) {
						String uuidMember = conn.getUUID(args[1]);
						int time = Integer.parseInt(args[2]);
						p.sendMessage(Main.prefix + StringGetterBau.getString(p, "memberTempAdded")
								.replace("%r", args[1]).replace("%t", String.valueOf(time)));
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionManager regions = container
								.get(BukkitAdapter.adapt(Bukkit.getWorld(p.getUniqueId().toString())));

						for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
							DefaultDomain member = rg.getValue().getMembers();
							member.addPlayer(UUID.fromString(uuidMember));
							rg.getValue().setMembers(member);
						}
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
			conn.closeConn();
			return true;
		} else {
			conn.closeConn();
			return false;
		}
		conn.closeConn();
		return false;
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
							conn.removeMember(uuidOwner, conn.getName(uuidMember));
							// wg remove member
							RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
							RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(s)));

							for (Entry<String, ProtectedRegion> rg : regions.getRegions().entrySet()) {
								DefaultDomain member = rg.getValue().getMembers();
								member.removePlayer(UUID.fromString(uuidMember));
								rg.getValue().setMembers(member);
							}
							// config remove
							config.set(uuidOwner + "." + uuidMember, null);
							try {
								config.save(Main.getPlugin().getTempAddConfigFile());
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
							} catch (IOException e) {
								e.printStackTrace();
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
