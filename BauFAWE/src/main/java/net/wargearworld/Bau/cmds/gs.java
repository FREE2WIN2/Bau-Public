package net.wargearworld.Bau.cmds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Predicate;

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
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.Plots.Plots;
import net.wargearworld.Bau.Tools.GUI;
import net.wargearworld.Bau.World.BauWorld;
import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.HelperMethods;
import net.wargearworld.Bau.utils.JsonCreater;
import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.CommandNode;
import net.wargearworld.CommandManager.ParseState;
import net.wargearworld.CommandManager.Arguments.DynamicListArgument;
import net.wargearworld.CommandManager.Arguments.DynamicListGetter;
import net.wargearworld.CommandManager.Arguments.IntegerArgument;
import net.wargearworld.CommandManager.Arguments.StringArgument;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;

import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;
import static net.wargearworld.CommandManager.Nodes.OptionalNode.optional;
import static net.wargearworld.CommandManager.Nodes.InvisibleNode.invisible;
public class gs implements CommandExecutor {

	public static File logFile;
	private HashSet<UUID> firstWarnNewPlot = new HashSet<>();
	private HashSet<UUID> secondWarnNewPlot = new HashSet<>();
	private HashSet<UUID> blocked = new HashSet<>();

	
	private CommandHandel commandHandle;
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
		CommandNode timeAdd = argument("Zeit", new IntegerArgument());
		CommandNode playersToAdd = argument("Spieler", new DynamicListArgument("Spieler", new DynamicListGetter<String>() {
			
			@Override
			public Collection<String> getList(ParseState state) {
				List<String> out = new ArrayList<>();
				BauWorld world = WorldManager.get(state.getPlayer().getWorld());
				for(Player p: Bukkit.getOnlinePlayers()) {
					if(!world.isAuthorized(p.getUniqueId())) {
						out.add(p.getName());
					}
				}
				return out;
			}
		}));
		CommandNode members = argument("Mitglied", new DynamicListArgument("Mitglied", new DynamicListGetter<String>() {
			
			@Override
			public Collection<String> getList(ParseState state) {
				List<String> out = new ArrayList<>();
				BauWorld world = WorldManager.get(state.getPlayer().getWorld());
				for(Player p: Bukkit.getOnlinePlayers()) {
					if(world.isAuthorized(p.getUniqueId())&&!world.isOwner(p)) {
						out.add(p.getName());
					}
				}
				return out;
			}
		}));
		Predicate<ArgumentList> owner = s ->{return WorldManager.get(s.getPlayer().getWorld()).isOwner(s.getPlayer());};
		commandHandle = new CommandHandel("gs", Main.prefix);
		commandHandle.setCallback(s->{tp(s);});
		
		commandHandle.addSubNode(literal("new")
				.setCallback(s->{newPlot(s.getPlayer(), 1);})
				.addSubNode(invisible(optional(argument("UUID1",new StringArgument())))
						.setRequirement(s->{return s.getString("UUID1").equals(s.getPlayer().getUniqueId().toString());})
						.setCallback(s->{newPlot(s.getPlayer(), 2);})
						.addSubNode(optional(argument("UUID2",new StringArgument()))
								.setRequirement(s->{return s.getString("UUID2").equals(s.getPlayer().getUniqueId().toString());})
								.setCallback(s->{newPlot(s.getPlayer(), 3);}))));
		
		commandHandle.addSubNode(literal("info").setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).showInfo(s.getPlayer());}));
		commandHandle.addSubNode(literal("list").setCallback(s->{BauPlayer.getBauPlayer(s.getPlayer()).sendMemberedGS();}));
		
		commandHandle.addSubNode(literal("tp")
				.addSubNode(argument("Spielername", new StringArgument())
						.setCallback(s->{tp(s);})));
		
		commandHandle.addSubNode(literal("add")
				.setRequirement(owner)
				.addSubNode(playersToAdd
						.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).add(s.getString("Spieler"),null);})
						.addSubNode(optional(timeAdd)
								.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).addTemp(s.getString("Spieler"),s.getInt("Zeit"));}))));
		commandHandle.addSubNode(literal("addtemp")
				.setRequirement(owner)
				.addSubNode(playersToAdd
						.addSubNode(optional(timeAdd)
								.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).addTemp(s.getString("Spieler"),s.getInt("Zeit"));}))));
		commandHandle.addSubNode(literal("remove")
				.setRequirement(owner)
				.addSubNode(members
						.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).removeMember(UUID.fromString(DBConnection.getUUID(s.getString("Spieler"))),false);})));
	}

	private void tp(ArgumentList s) {
		Player p = s.getPlayer();
		String name = s.getString("Spielername");
		if(name == null) {
			WorldManager.getWorld(p.getUniqueId()).spawn(p);
		}else {
			WorldManager.getWorld(name).spawn(p);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

		Player p = (Player) sender;
		if (args.length == 0) {
			// gs -> tp zum own gs
//			p.teleport(CoordGetter.getTeleportLocation(WorldManager.loadWorld(p.getUniqueId().toString()),
//					Plots.getJoinPlot(p.getUniqueId())));
//			return true;
		} else if (args.length == 1) {
//			if (args[0].equalsIgnoreCase("list")) {
//				// gs list
//				ArrayList<String> memberedPlots = new ArrayList<>();
//				memberedPlots.add(p.getName());
//				memberedPlots.addAll(DBConnection.getMemberedPlots(p.getUniqueId()));
//				sendMemberedGs(p, memberedPlots);
//				return true;
//
//			}
//			if (args[0].equalsIgnoreCase("info")) {
//				// bau info
//				// open gui
//				GUI.showMember(p);
//				Main.send(p, "timeShow", p.getWorld().getTime() + "");
//				return true;
//
//			}
//			if (args[0].equalsIgnoreCase("new")) {
//				/* erste WArnung, dass GS gelöscht wird -> einschreiben in Liste */
//				newPlot(p, 1);
//				return true;
//			}
		} else if (args.length == 2) {
			switch (args[0].toLowerCase()) {
			case "time":
				setTime(p, Integer.parseInt(args[1]));
				break;
//			case "addtemp":
//				p.sendMessage(Main.prefix
//						+ MessageHandler.getInstance().getString(p, "wrongCommand").replace("%r", "gs addtemp <Spieler> <Zeit(h)>"));
//				break;
//			case "tempadd":
//				p.sendMessage(Main.prefix
//						+ MessageHandler.getInstance().getString(p, "wrongCommand").replace("%r", "gs tempadd <Spieler> <Zeit(h)>"));
//				break;
			case "remove":
				removeMember(p, args[1]);
				// bau remove [PlayerName]
				break;
//			case "add":
//				addMember(p, args[1]);
//				// Befehle: /bau add [PlayerName]
//				break;
//			case "tp":
//				// if (conn.isMember(p, args[1]) || p.hasPermission("moderator")) {
//				if ((DBConnection.isMember(p.getUniqueId(), args[1]) || p.hasPermission("moderator"))
//						&& DBConnection.hasOwnPlots(args[1])) {
//					String plotID = DBConnection.getUUID(args[1]); // == UUID of the Owner
//					p.teleport(CoordGetter.getTeleportLocation(WorldManager.loadWorld(plotID),
//							Plots.getJoinPlot(UUID.fromString(plotID))));
//
//				} else {
//					p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "noPlotMember"));
//				}
//				// bau tp [Playername]
//				break;
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
//			case "add":
//			case "addtemp":
//			case "tempadd":
//				addMemberTemp(p, args[1], Integer.parseInt(args[2]));
//				break;
//			case "new":
//				if (args[1].equals(p.getUniqueId().toString()) && args[2].equals(p.getUniqueId().toString())) {
//					newPlot(p, 3);
//				}
//				break;
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
			JsonCreater jsonMsg = new JsonCreater(Main.prefix + MessageHandler.getInstance().getString(p, "gs_newPlot_secondWarn"));
			JsonCreater jsonMsgClick = new JsonCreater(MessageHandler.getInstance().getString(p, "gs_newPlot_secondWarn_click"));
			jsonMsgClick.addHoverEvent(MessageHandler.getInstance().getString(p, "gs_newPlot_secondWarn_clickHover"));
			jsonMsg.addJson(jsonMsgClick.addClickEvent("/gs new " + uuid.toString() + " " + uuid.toString(),
					ClickAction.RUN_COMMAND)).send(p);

			secondWarnNewPlot.add(uuid);
		} else if (argsLength == 1) {
			JsonCreater jsonMsg = new JsonCreater(Main.prefix + MessageHandler.getInstance().getString(p, "gs_newPlot_firstWarn"));
			JsonCreater jsonMsgClick = new JsonCreater(MessageHandler.getInstance().getString(p, "gs_newPlot_firstWarn_click"));
			jsonMsgClick.addHoverEvent(MessageHandler.getInstance().getString(p, "gs_newPlot_firstWarn_clickHover"));
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

	public void sendMemberedGs(Player p, ArrayList<String> memberedPlots) {
		PlayerConnection pConn = ((CraftPlayer) p).getHandle().playerConnection;
		p.sendMessage(MessageHandler.getInstance().getString(p, "listGsHeading"));
		for (String s : memberedPlots) {
			/* s == PlayerName */
			String hover = MessageHandler.getInstance().getString(p, "listGsHover").replace("%r", s);
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
		World w = WorldManager.loadWorld(worldName);
		for (Player a : w.getPlayers()) {
			a.kickPlayer("GS DELETE");
		}
		if (WorldManager.deleteWorld(w) && !mute) {
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
