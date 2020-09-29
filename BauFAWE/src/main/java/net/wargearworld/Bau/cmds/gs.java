package net.wargearworld.Bau.cmds;

import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.InvisibleNode.invisible;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;
import static net.wargearworld.CommandManager.Requirements.PermissionRequirement.permission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.World.BauWorld;
import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.JsonCreater;
import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.CommandNode;
import net.wargearworld.CommandManager.ParseState;
import net.wargearworld.CommandManager.Arguments.DynamicListArgument;
import net.wargearworld.CommandManager.Arguments.DynamicListGetter;
import net.wargearworld.CommandManager.Arguments.IntegerArgument;
import net.wargearworld.CommandManager.Arguments.StringArgument;
public class gs implements TabExecutor {

	public static File logFile;
	private HashSet<UUID> firstWarnNewPlot = new HashSet<>();
	private HashSet<UUID> secondWarnNewPlot = new HashSet<>();
	private HashSet<UUID> blocked = new HashSet<>();

	
	private CommandHandel commandHandle;
	public gs() {
		Main.getPlugin().getCommand("gs").setExecutor(this);
		Main.getPlugin().getCommand("gs").setTabCompleter(this);
		
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
				BauWorld world = WorldManager.get(state.getPlayer().getWorld());
				return DBConnection.getAllNotAddedPlayers(world.getId()).values();
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
		
CommandNode worlds = argument("Worlds", new DynamicListArgument("Worlds", new DynamicListGetter<String>() {
			
			@Override
			public Collection<String> getList(ParseState state) {
				return DBConnection.getAllWorlds();
			}
		}));
		Predicate<ArgumentList> owner = s ->{return WorldManager.get(s.getPlayer().getWorld()).isOwner(s.getPlayer());};
		Predicate<ArgumentList> authorised = s ->{return getWorld(s).isAuthorized(s.getPlayer().getUniqueId()) || s.getPlayer().hasPermission("moderator");};
		commandHandle = new CommandHandel("gs", Main.prefix);
		commandHandle.setCallback(s->{tp(s);});
		
		commandHandle.addSubNode(literal("new")
				.setCallback(s->{newPlot(s.getPlayer(), 1);})
				.addSubNode(invisible(argument("UUID1",new StringArgument()))
						.setRequirement(s->{return s.getString("UUID1").equals(s.getPlayer().getUniqueId().toString());})
						.setCallback(s->{newPlot(s.getPlayer(), 2);})
						.addSubNode(argument("UUID2",new StringArgument())
								.setRequirement(s->{return s.getString("UUID2").equals(s.getPlayer().getUniqueId().toString());})
								.setCallback(s->{newPlot(s.getPlayer(), 3);}))));
		
		commandHandle.addSubNode(literal("info").setCallback(s->{getWorld(s).showInfo(s.getPlayer());}));
		commandHandle.addSubNode(literal("list").setCallback(s->{BauPlayer.getBauPlayer(s.getPlayer()).sendMemberedGS();}));
		
		commandHandle.addSubNode(literal("tp")
				.addSubNode(argument("Spielername", new StringArgument())
						.setCallback(s->{tp(s);})));
		/* gs add <Spieler> [Zeit]*/
		commandHandle.addSubNode(literal("add")
				.setRequirement(owner)
				.addSubNode(playersToAdd
						.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).add(s.getString("Spieler"),null);})
						.addSubNode(timeAdd
								.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).addTemp(s.getString("Spieler"),s.getInt("Zeit"));}))));
		/* gs addTemp <Spieler> [Zeit]*/
		commandHandle.addSubNode(literal("addtemp")
				.setRequirement(owner)
				.addSubNode(playersToAdd
						.addSubNode(timeAdd
								.setCallback(s->{WorldManager.get(s.getPlayer().getWorld()).addTemp(s.getString("Spieler"),s.getInt("Zeit"));}))));
		/* gs remove <Spieler>*/
		commandHandle.addSubNode(literal("remove")
				.setRequirement(owner)
				.addSubNode(members
						.setCallback(s->{getWorld(s).removeMember(UUID.fromString(DBConnection.getUUID(s.getString("Spieler"))));})));
		/* gs time [Zeit]*/
		commandHandle.addSubNode(literal("time")
				.addSubNode(argument("Zeit",new IntegerArgument(1,24000))
						.setRequirement(authorised)
						.setCallback(s->{getWorld(s).setTime(s.getInt("Zeit"));Main.send(s.getPlayer(), "time", "" + s.getInt("Zeit"));})));
		/* gs delete <World>*/
		commandHandle.addSubNode(literal("delete")
				.setRequirement(permission("bau.delete.bypass")).addSubNode(worlds.setCallback(s->{deletePlot(s);})));

	}

	private BauWorld getWorld(ArgumentList s) {
		return WorldManager.get(s.getPlayer().getWorld());
	}

	private void tp(ArgumentList s) {
		Player p = s.getPlayer();
		String name = s.getString("Spielername");
		if(name == null) {
			WorldManager.getWorld(p.getName(),p.getUniqueId().toString()).spawn(p);
		}else {
			WorldManager.getWorld(name).spawn(p);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command arg1,
			String arg2, String[] args) {
		Player p = (Player) sender;
		List<String> ret = new ArrayList<>();
		commandHandle.tabComplete(p, MessageHandler.getInstance().getLanguage(p), args,ret);
		return ret;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		Player p = (Player) sender;
		return commandHandle.execute(p, MessageHandler.getInstance().getLanguage(p), args);
	}

	private void newPlot(Player p, int argsLength) {
		UUID uuid = p.getUniqueId();
		if (blocked.contains(uuid)) {
			Main.send(p, "gs_newPlotGeneration_antiSpam");
			return;
		}
		if (secondWarnNewPlot.contains(uuid) && argsLength == 3) {
			Main.send(p, "gs_newPlotGenerating");
			BauWorld world = WorldManager.get(p.getWorld());
			world.newWorld();
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

	public void deletePlot(ArgumentList s) {
		BauWorld world = WorldManager.getWorld(s.getString("Worlds"));
		if (world.newWorld()) {
			Main.send(s.getPlayer(), "gsDeleted", s.getString("worlds"));
		}
	}

	
}
