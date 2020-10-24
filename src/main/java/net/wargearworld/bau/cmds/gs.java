package net.wargearworld.bau.cmds;

import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.Arguments.DynamicListArgument;
import net.wargearworld.CommandManager.Arguments.DynamicListGetter;
import net.wargearworld.CommandManager.Arguments.IntegerArgument;
import net.wargearworld.CommandManager.Arguments.StringArgument;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.CommandNode;
import net.wargearworld.CommandManager.ParseState;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static net.wargearworld.CommandManager.Arguments.DynamicListArgument.dynamicList;
import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.InvisibleNode.invisible;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;
import static net.wargearworld.CommandManager.Requirements.PermissionRequirement.permission;

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
                if (world instanceof PlayerWorld) {
                    return DBConnection.getAllNotAddedPlayers(world.getId());
                }
                return new ArrayList<>();
            }
        }));
        CommandNode members = argument("Mitglied", new DynamicListArgument("Mitglied", new DynamicListGetter<String>() {

            @Override
            public Collection<String> getList(ParseState state) {
                TreeSet<String> out = new TreeSet<>();
                BauWorld world = WorldManager.get(state.getPlayer().getWorld());
                if (world instanceof PlayerWorld) {
                    PlayerWorld playerWorld = (PlayerWorld) world;
                    out.addAll(playerWorld.getMemberNames());
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
        Predicate<ArgumentList> owner = s -> {
            return WorldManager.get(s.getPlayer().getWorld()).isOwner(s.getPlayer());
        };
        Predicate<ArgumentList> authorised = s -> {
            return getWorld(s).isAuthorized(s.getPlayer().getUniqueId()) || s.getPlayer().hasPermission("moderator");
        };
        commandHandle = new CommandHandel("gs", Main.prefix, Main.getPlugin());
        commandHandle.setCallback(s -> {
            tp(s);
        });

        commandHandle.addSubNode(literal("new")
                .setCallback(s -> {
                    newPlot(s.getPlayer(), 1, s);
                })
                .addSubNode(invisible(argument("UUID1", dynamicList("UUID1", s -> {
                    return List.of(s.getPlayer().getUniqueId().toString());
                }))
                        .setCallback(s -> {
                            newPlot(s.getPlayer(), 2, s);
                        })
                        .addSubNode(
                                argument("UUID2", dynamicList("UUID2", s -> {
                                    return List.of(s.getPlayer().getUniqueId().toString());
                                }))
                                        .setCallback(s -> {
                                            newPlot(s.getPlayer(), 3, s);
                                        })))));

        commandHandle.addSubNode(literal("info").setCallback(s -> {
            getWorld(s).showInfo(s.getPlayer());
        }));
        commandHandle.addSubNode(literal("list").setCallback(s -> {
            BauPlayer.getBauPlayer(s.getPlayer()).sendMemberedGS();
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
                            WorldManager.get(s.getPlayer().getWorld()).add(s.getString("Spieler"), null);
                        })
                        .addSubNode(timeAdd
                                .setCallback(s -> {
                                    WorldManager.get(s.getPlayer().getWorld()).addTemp(s.getString("Spieler"), s.getInt("Zeit"));
                                }))));
        /* gs addTemp <Spieler> [Zeit]*/
        commandHandle.addSubNode(literal("addtemp")
                .setRequirement(owner)
                .addSubNode(playersToAdd
                        .addSubNode(timeAdd
                                .setCallback(s -> {
                                    WorldManager.get(s.getPlayer().getWorld()).addTemp(s.getString("Spieler"), s.getInt("Zeit"));
                                }))));
        /* gs remove <Spieler>*/
        commandHandle.addSubNode(literal("remove")
                .setRequirement(owner)
                .addSubNode(members
                        .setCallback(s -> {
                            getWorld(s).removeMember(DBConnection.getPlayer(s.getString("Mitglied")).getUuid());
                        })));
        /* gs time [Zeit]*/
        commandHandle.addSubNode(literal("time")
                .addSubNode(argument("Zeit", new IntegerArgument(1, 24000))
                        .setRequirement(authorised)
                        .setCallback(s -> {
                            getWorld(s).setTime(s.getInt("Zeit"));
                            Main.send(s.getPlayer(), "time", "" + s.getInt("Zeit"));
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
                        return WorldManager.get(s.getPlayer().getWorld()).isOwner(s.getPlayer());
                    })
                    .addSubNode(members.clone().setCallback(s -> {
                        rights(s, true);
                    })));
            /* gs setrights <Spieler> */
            commandHandle.addSubNode(literal("removerights")
                    .setRequirement(s -> {
                        BauWorld bauWorld = WorldManager.get(s.getPlayer().getWorld());
                        return bauWorld instanceof PlayerWorld && bauWorld.isOwner(s.getPlayer());
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
        UUID memberUUID = DBConnection.getUUID(memberName);
        Player p = s.getPlayer();
        EntityManager em = DependencyProvider.getEntityManager();
        em.getTransaction().begin();
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        long id = bauWorld.getId();
        Plot dbPlot = em.find(Plot.class, id);
        net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class, memberUUID);
        PlotMember plotMember = dbPlot.getMember(dbPlayer);
        plotMember.setRights(b);
        em.getTransaction().commit();
        em.close();


        if (b) {
            MessageHandler.getInstance().send(p, "plotrights_setted", memberName);
            bauWorld.addPlayerToAllRegions(memberUUID);
        } else {
            MessageHandler.getInstance().send(p, "plotrights_removed", memberName);
            bauWorld.addPlayerToAllRegions(memberUUID);
        }
    }

    private BauWorld getWorld(ArgumentList s) {
        return WorldManager.get(s.getPlayer().getWorld());
    }

    private void tp(ArgumentList s) {
        Player p = s.getPlayer();
        String name = s.getString("Spielername");
        if (name == null) {
            WorldManager.getWorld(p.getName(), p.getUniqueId().toString()).spawn(p);
        } else {
            net.wargearworld.db.model.Player owner = DBConnection.getPlayer(name);
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
        commandHandle.tabComplete(p, MessageHandler.getInstance().getLanguage(p), args, ret);
        return ret;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player p = (Player) sender;
        return commandHandle.execute(p, MessageHandler.getInstance().getLanguage(p), args);
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
            Main.send(s.getPlayer(), "gsDeleted", s.getString("worlds"));
        }
    }


}
