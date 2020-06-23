package net.wargearworld.Bau.cmds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.StringGetterBau;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Plots.Plots;
import net.wargearworld.Bau.Tools.GUI;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.HelperMethods;
import net.wargearworld.Bau.utils.JsonCreater;
import net.wargearworld.Bau.utils.WorldHandler;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;

public class gs implements CommandExecutor {

	public static File logFile;
	private HashSet<UUID> firstWarnNewPlot = new HashSet<>();
	private HashSet<UUID> secondWarnNewPlot = new HashSet<>();
	private HashSet<UUID> blocked = new HashSet<>();

	public gs() {
		logFile = new File(Main.getPlugin().getDataFolder(), "GsMemberedLog.txt");
		if (!logFile.exists()) {
			try {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

		Player p = (Player) sender;
		if (args.length == 0) {
			// gs -> tp zum own gs
			p.teleport(CoordGetter.getTeleportLocation(WorldHandler.loadWorld(p.getUniqueId().toString()),
					Plots.getJoinPlot(p.getUniqueId())));
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				// gs list
				ArrayList<String> memberedPlots = new ArrayList<>();
				memberedPlots.add(p.getName());
				memberedPlots.addAll(DBConnection.getMemberedPlots(p.getUniqueId()));
				sendMemberedGs(p, memberedPlots);
				return true;

			}
			if (args[0].equalsIgnoreCase("info")) {
				// bau info
				// open gui
				GUI.showMember(p);
				Main.send(p, "timeShow", p.getWorld().getTime() + "");
				return true;

			}
			if (args[0].equalsIgnoreCase("new")) {
				/* erste WArnung, dass GS gelöscht wird -> einschreiben in Liste */
				newPlot(p, 1);
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
				removeMember(p, args[1]);
				// bau remove [PlayerName]
				break;
			case "add":
				addMember(p, args[1]);
				// Befehle: /bau add [PlayerName]
				break;
			case "tp":
				// if (conn.isMember(p, args[1]) || p.hasPermission("moderator")) {
				if ((DBConnection.isMember(p.getUniqueId(), args[1]) || p.hasPermission("moderator"))
						&& DBConnection.hasOwnPlots(args[1])) {
					String plotID = DBConnection.getUUID(args[1]); // == UUID of the Owner
					p.teleport(CoordGetter.getTeleportLocation(WorldHandler.loadWorld(plotID),
							Plots.getJoinPlot(UUID.fromString(plotID))));

				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noPlotMember"));
				}
				// bau tp [Playername]
				break;
			case "delete":
				if (p.hasPermission("admin")) {
					deletePlot(p, args[1], false);
				} else {
					return false;
				}
				break;
			case "new":
				if (args[1].equals(p.getUniqueId().toString())) {
					newPlot(p, 2);
				}
				break;
			}
			return true;

		} else if (args.length == 3) {
			switch (args[0].toLowerCase()) {
			case "time":
			case "zeit":
				if (args[1].equalsIgnoreCase("set")) {
					setTime(p, Integer.parseInt(args[2]));
				} else {
					Main.send(p, "wrongCommand", "gs time set <time>");
				}
				break;
			case "add":
			case "addtemp":
			case "tempadd":
				addMemberTemp(p, args[1], Integer.parseInt(args[2]));
				break;
			case "new":
				if (args[1].equals(p.getUniqueId().toString()) && args[2].equals(p.getUniqueId().toString())) {
					newPlot(p, 3);
				}
				break;
			}
			return true;
		} else {
			return false;
		}
		return false;
	}

	private void newPlot(Player p, int argsLength) {
		UUID uuid = p.getUniqueId();
		if (blocked.contains(uuid)) {
			Main.send(p, "gs_newPlotGeneration_antiSpam");
			return;
		}
		if (secondWarnNewPlot.contains(uuid) && argsLength == 3) {
			Main.send(p, "gs_newPlotGenerating");
			deletePlot(p, p.getName(), true);
			writeLog("World New: " + p.getName() + "(" +p.getUniqueId().toString()+ ")");
			secondWarnNewPlot.remove(uuid);
			firstWarnNewPlot.remove(uuid);
			blocked.add(uuid);
		} else if (firstWarnNewPlot.contains(uuid) && argsLength == 2) {
			JsonCreater jsonMsg = new JsonCreater(Main.prefix + StringGetterBau.getString(p, "gs_newPlot_secondWarn"));
			JsonCreater jsonMsgClick = new JsonCreater(StringGetterBau.getString(p, "gs_newPlot_secondWarn_click"));
			jsonMsgClick.addHoverEvent(StringGetterBau.getString(p, "gs_newPlot_secondWarn_clickHover"));
			jsonMsg.addJson(jsonMsgClick.addClickEvent("/gs new " + uuid.toString() + " " + uuid.toString(),
					ClickAction.RUN_COMMAND)).send(p);

			secondWarnNewPlot.add(uuid);
		} else if (argsLength == 1) {
			JsonCreater jsonMsg = new JsonCreater(Main.prefix + StringGetterBau.getString(p, "gs_newPlot_firstWarn"));
			JsonCreater jsonMsgClick = new JsonCreater(StringGetterBau.getString(p, "gs_newPlot_firstWarn_click"));
			jsonMsgClick.addHoverEvent(StringGetterBau.getString(p, "gs_newPlot_firstWarn_clickHover"));
			jsonMsg.addJson(jsonMsgClick.addClickEvent("/gs new " + uuid.toString(), ClickAction.RUN_COMMAND)).send(p);

			firstWarnNewPlot.add(uuid);
		}

	}

	private void setTime(Player p, int time) {
		if (time > 24000) {
			Main.send(p, "timeTooHigh", "" + time);
		} else {
			p.getWorld().setTime(time);
			Main.send(p, "time", "" + time);
		}

	}

	public void removeMember(Player p, String playerNameToRemove) {
		if (!p.getName().equalsIgnoreCase(playerNameToRemove)) {
			UUID membetUUID = UUID.fromString(DBConnection.getUUID(playerNameToRemove));
			if (Plots.getPlot(p.getUniqueId()).removeMember(membetUUID.toString(),
					playerNameToRemove)) {
				writeLog(playerNameToRemove + "("+membetUUID.toString()+") removed from " + p.getName() + "'s("+p.getUniqueId().toString()+") plot at " + HelperMethods.getTime());
			} else {
				Main.send(p, "error");
			}
		} else {
			Main.send(p, "YouCantRemoveYourself");
		}
	}

	public boolean addMember(Player p, String playerName) {
		if (Plots.getPlot(p.getUniqueId()).isMember(UUID.fromString(DBConnection.getUUID(playerName)))) {
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "alreadyMember").replace("%r", playerName));
			return false;
		} else {
			String memberUUID = DBConnection.getUUID(playerName);
			if (Plots.getPlot(p.getUniqueId()).addMember(memberUUID)) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "plotMemberAdded").replace("%r", playerName));
				writeLog(playerName + "("+memberUUID+") added to " + p.getName() + "'s("+p.getUniqueId().toString()+") plot at " + HelperMethods.getTime());
				return true;
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
				return false;
			}
		}
	}

	public void addMemberTemp(Player p, String playerName, int time) {
		if (DBConnection.isMember(UUID.fromString(DBConnection.getUUID(playerName)), p.getName())) {
			Main.send(p, "alreadyMember", playerName);
		} else {
			String uuidMember = DBConnection.getUUID(playerName);
			if (Plots.getPlot(p.getUniqueId()).addMember(uuidMember)) {
				Main.send(p, "memberTempAdded", playerName, "" + time);

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
				writeLog(playerName + "("+uuidMember+") tempadded for " + time + " hours to " + p.getName() + "'s("+p.getUniqueId().toString()+") plot at " + HelperMethods.getTime());
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "error"));
			}
		}
	}

	public void sendMemberedGs(Player p, ArrayList<String> memberedPlots) {
		PlayerConnection pConn = ((CraftPlayer) p).getHandle().playerConnection;
		p.sendMessage(StringGetterBau.getString(p, "listGsHeading"));
		for (String s : memberedPlots) {
			/* s == PlayerName */
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
	}

	public void deletePlot(Player p, String playerName, boolean mute) {
		String worldName = DBConnection.getUUID(playerName);
		World w = WorldHandler.loadWorld(worldName);
		for (Player a : w.getPlayers()) {
			a.kickPlayer("GS DELETE");
		}
		if (WorldHandler.deleteWorld(w) && !mute) {
			Main.send(p, "gsDeleted", playerName);
			writeLog("World Deleted: " + p.getName() + "(" +p.getUniqueId().toString()+ ")");
		} else if(!mute) {
			Main.send(p, "error");
		}
		
	}

	public static void startCheckForTempAdd() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				Main.tempAddConfigFile = Main.createConfigFile("tempAddConfig.yml");
				Main.tempAddConfig = Main.createConfig(Main.tempAddConfigFile);
				YamlConfiguration config = Main.tempAddConfig;
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
							if(DBConnection.isMember(UUID.fromString(uuidMember), DBConnection.getName(uuidOwner))) {
								if (!Plots.getPlot(UUID.fromString(uuidOwner)).removeMember(m,
										DBConnection.getName(uuidMember))) {
									System.err.println("Member konnte nicht entfernt werden : MemberName: " + DBConnection.getName(uuidMember)
									+ " | OwnerUUID: " + uuidOwner);
									
									/* Log */
									String memberName = DBConnection.getName(uuidMember);
									String ownerName = DBConnection.getName(uuidOwner);

									writeLog(memberName + "("+uuidMember + ")removed from " + ownerName + "'s("+uuidOwner+") plot at "
											+ HelperMethods.getTime());
									return;
								}								
							}
							// config remove
							config.set(uuidOwner + "." + uuidMember, null);
							try {
								config.save(Main.getPlugin().getTempAddConfigFile());
							} catch (IOException e) {
								e.printStackTrace();
								return;
							}

							

							// output?
						}

					}
				}
			}
		}, 0, 5 * 60 * 20);// 30*60*

	}

	public static void writeLog(String message) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(message);
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
