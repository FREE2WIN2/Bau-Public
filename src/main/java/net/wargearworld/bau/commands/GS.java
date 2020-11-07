package net.wargearworld.bau.commands;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.CommandNode;
import net.wargearworld.command_manager.ParseState;
import net.wargearworld.command_manager.arguments.DynamicListGetter;
import net.wargearworld.command_manager.arguments.StringArgument;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static net.wargearworld.bau.utils.CommandUtil.getPlayer;
import static net.wargearworld.command_manager.arguments.DynamicListArgument.dynamicList;
import static net.wargearworld.command_manager.arguments.IntegerArgument.integer;
import static net.wargearworld.command_manager.nodes.ArgumentNode.argument;
import static net.wargearworld.command_manager.nodes.InvisibleNode.invisible;
import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.command_manager.requirements.PermissionRequirement.permission;

public class GS implements TabExecutor {

    public static File logFile;
    private HashSet<UUID> firstWarnNewPlot = new HashSet<>();
    private HashSet<UUID> secondWarnNewPlot = new HashSet<>();
    private HashSet<UUID> blocked = new HashSet<>();


    private CommandHandel commandHandle;

    public GS() {
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
        CommandNode timeAdd = argument("Zeit", integer());
        CommandNode playersToAdd = argument("Spieler", dynamicList("Spieler", new DynamicListGetter<String>() {

            @Override
            public Collection<String> getList(ParseState state) {
                BauWorld world = WorldManager.get(getPlayer(state.getArgumentList()).getWorld());
                if (world instanceof PlayerWorld) {
                    return DatabaseDAO.getAllNotAddedPlayers(world.getId());
                }
                return new ArrayList<>();
            }
        }));
        CommandNode members = argument("Mitglied", dynamicList("Mitglied", new DynamicListGetter<String>() {

            @Override
            public Collection<String> getList(ParseState state) {
                TreeSet<String> out = new TreeSet<>();
                BauWorld world = WorldManager.get(getPlayer(state.getArgumentList()).getWorld());
                if (world instanceof PlayerWorld) {
                    PlayerWorld playerWorld = (PlayerWorld) world;
                    out.addAll(playerWorld.getMemberNames());
                }
                return out;
            }
        }));

        CommandNode worlds = argument("Worlds", dynamicList("Worlds", new DynamicListGetter<String>() {

            @Override
            public Collection<String> getList(ParseState state) {

                return DatabaseDAO.getAllWorlds();

            }
        }));
        Predicate<ArgumentList> owner = s -> {
            Player p = getPlayer(s);
            return WorldManager.get(p.getWorld()).isOwner(p);
        };
        Predicate<ArgumentList> authorised = s -> {
            return getWorld(s).isAuthorized(getPlayer(s).getUniqueId()) || getPlayer(s).hasPermission("moderator");
        };
        commandHandle = new CommandHandel("gs", Main.prefix, MessageHandler.getInstance());
        commandHandle.setCallback(s -> {
            tp(s);
        });

        commandHandle.addSubNode(literal("new")
                .setCallback(s -> {
                    newPlot(getPlayer(s), 1, s);
                })
                .addSubNode(invisible(argument("UUID1", dynamicList("UUID1", s -> {
                    return List.of(getPlayer(s.getArgumentList()).getUniqueId().toString());
                }))
                        .setCallback(s -> {
                            newPlot(getPlayer(s), 2, s);
                        })
                        .addSubNode(
                                argument("UUID2", dynamicList("UUID2", s -> {
                                    return List.of(getPlayer(s.getArgumentList()).getUniqueId().toString());
                                }))
                                        .setCallback(s -> {
                                            newPlot(getPlayer(s), 3, s);
                                        })))));

        commandHandle.addSubNode(literal("info").setCallback(s -> {
            getWorld(s).showInfo(getPlayer(s));
        }));
        commandHandle.addSubNode(literal("list").setCallback(s -> {
            BauPlayer.getBauPlayer(getPlayer(s)).sendMemberedGS();
        }));

        commandHandle.addSubNode(literal("tp")
                .addSubNode(argument("Spielername", new StringArgument())
                        .setCallback(s -> {
                            tp(s);
                        })));
        /* gs add <Spieler> [Zeit]*/
        commandHandle.addSubNode(literal("add")
                .setRequirement(owner)
                .addSubNode(playersToAdd
                        .setCallback(s -> {
                            WorldManager.get(getPlayer(s).getWorld()).add(s.getString("Spieler"), null);
                        })
                        .addSubNode(timeAdd
                                .setCallback(s -> {
                                    WorldManager.get(getPlayer(s).getWorld()).addTemp(s.getString("Spieler"), s.getInt("Zeit"));
                                }))));
        /* gs addTemp <Spieler> [Zeit]*/
        commandHandle.addSubNode(literal("addtemp")
                .setRequirement(owner)
                .addSubNode(playersToAdd
                        .addSubNode(timeAdd
                                .setCallback(s -> {
                                    WorldManager.get(getPlayer(s).getWorld()).addTemp(s.getString("Spieler"), s.getInt("Zeit"));
                                }))));
        /* gs remove <Spieler>*/
        commandHandle.addSubNode(literal("remove")
                .setRequirement(owner)
                .addSubNode(members
                        .setCallback(s -> {
                            getWorld(s).removeMember(DatabaseDAO.getPlayer(s.getString("Mitglied")).getUuid());
                        })));
        /* gs time [Zeit]*/
        commandHandle.addSubNode(literal("time")
                .addSubNode(argument("Zeit", integer(1, 24000))
                        .setRequirement(authorised)
                        .setCallback(s -> {
                            getWorld(s).setTime(s.getInt("Zeit"));
                            Main.send(getPlayer(s), "time", "" + s.getInt("Zeit"));
                        })));
        /* gs delete <World>*/
        commandHandle.addSubNode(literal("delete")
                .setRequirement(permission("bau.delete.bypass")).addSubNode(worlds.setCallback(s -> {
                    deletePlot(s);
                })));
        try {
            /* gs setrights <Spieler> */
            commandHandle.addSubNode(literal("setrights")
                    .setRequirement(s -> {
                        return WorldManager.get(getPlayer(s).getWorld()).isOwner(getPlayer(s));
                    })
                    .addSubNode(members.clone().setCallback(s -> {
                        rights(s, true);
                    })));
            /* gs setrights <Spieler> */
            commandHandle.addSubNode(literal("removerights")
                    .setRequirement(s -> {
                        BauWorld bauWorld = WorldManager.get(getPlayer(s).getWorld());
                        return bauWorld instanceof PlayerWorld && bauWorld.isOwner(getPlayer(s));
                    })
                    .addSubNode(members.clone().setCallback(s -> {
                        rights(s, false);
                    })));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void rights(ArgumentList s, boolean b) {
        String memberName = s.getString("Mitglied");
        UUID memberUUID = DatabaseDAO.getUUID(memberName);
        Player p = getPlayer(s);

        EntityManagerExecuter.run(em -> {
            BauWorld bauWorld = WorldManager.get(p.getWorld());
            long id = bauWorld.getId();
            Plot dbPlot = em.find(Plot.class, id);
            net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class, memberUUID);
            PlotMember plotMember = dbPlot.getMember(dbPlayer);
            plotMember.setRights(b);
            if (b) {
                MessageHandler.getInstance().send(p, "plotrights_setted", memberName);
                bauWorld.addPlayerToAllRegions(memberUUID);
            } else {
                MessageHandler.getInstance().send(p, "plotrights_removed", memberName);
                bauWorld.addPlayerToAllRegions(memberUUID);
            }
        });


    }

    private BauWorld getWorld(ArgumentList s) {
        return WorldManager.get(getPlayer(s).getWorld());
    }

    private void tp(ArgumentList s) {
        Player p = getPlayer(s);
        String name = s.getString("Spielername");
        if (name == null) {
            WorldManager.getWorld(p.getName(), p.getUniqueId().toString()).spawn(p);
        } else {
            net.wargearworld.db.model.Player owner = DatabaseDAO.getPlayer(name);
            if (owner == null) {
                return; //TODO error
            }
            WorldManager.getWorld(name, owner.getUuid().toString()).spawn(p);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command arg1,
                                      String arg2, String[] args) {
        Player p = (Player) sender;
        List<String> ret = new ArrayList<>();
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandle.tabComplete(commandPlayer, MessageHandler.getInstance().getLanguage(p), args, ret);
        return ret;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player p = (Player) sender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        return commandHandle.execute(commandPlayer, MessageHandler.getInstance().getLanguage(p), args);
    }

    private void newPlot(Player p, int argsLength, ArgumentList s) {
        UUID uuid = p.getUniqueId();
        if (blocked.contains(uuid)) {
            Main.send(p, "gs_newPlotGeneration_antiSpam");
            return;
        }
        if (secondWarnNewPlot.contains(uuid) && argsLength == 3 && p.getUniqueId().toString().equals(s.getString("UUID2"))) {
            Main.send(p, "gs_newPlotGenerating");
            BauWorld world = WorldManager.get(p.getWorld());
            world.newWorld();
            secondWarnNewPlot.remove(uuid);
            firstWarnNewPlot.remove(uuid);
            blocked.add(uuid);
        } else if (firstWarnNewPlot.contains(uuid) && argsLength == 2 && p.getUniqueId().toString().equals(s.getString("UUID1"))) {
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
            Main.send(getPlayer(s), "gsDeleted", s.getString("worlds"));
        }
    }


}
