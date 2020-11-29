package net.wargearworld.bau.commands;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.dao.WorldDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.team.TeamManager;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.CommandNode;
import net.wargearworld.command_manager.ParseState;
import net.wargearworld.command_manager.arguments.DynamicListGetter;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.World;
import net.wargearworld.db.model.WorldMember;
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
                    return WorldDAO.getAllNotAddedPlayers(world.getId());
                }
                return new ArrayList<>();
            }
        }));
        CommandNode members = argument("Mitglied", dynamicList("Mitglied", state -> {
            TreeSet<String> out = new TreeSet<>();
            BauWorld world = WorldManager.get(getPlayer(state.getArgumentList()).getWorld());
            if (world instanceof PlayerWorld) {
                PlayerWorld playerWorld = (PlayerWorld) world;
                out.addAll(playerWorld.getMemberNames());
            }
            return out;
        }));

        CommandNode worlds = argument("Worlds", dynamicList("Worlds", new DynamicListGetter<String>() {

            @Override
            public Collection<String> getList(ParseState state) {

                return WorldDAO.getAllWorlds();

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
            tp(s, true);
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
                                argument("UUID2", dynamicList("UUID2", state -> {
                                    return List.of(getPlayer(state.getArgumentList()).getUniqueId().toString());
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

        commandHandle.addSubNode(literal("team").setCallback(s -> {
            tpTeam(s);
        }));
        commandHandle.addSubNode(literal("tp")
                .addSubNode(literal("team").setCallback(s -> {
                    tpTeam(s);
                }))
                .addSubNode(argument("Worldname", dynamicList("Worldname", state -> {
                    return PlayerDAO.getPlayersWorldNames(getPlayer(state.getArgumentList()).getName());
                }))
                        .setCallback(s -> {
                            tp(s, false);
                        }))
                .addSubNode(argument("Spielername", dynamicList("Spielername", state -> {
                    if (getPlayer(state.getArgumentList()).hasPermission("bau.move.bypass"))
                        return PlayerDAO.getAllPlayersWithWorld();
                    return PlayerDAO.getPlayersAddedWorldsPlayerNames(state.getPlayer().getUUID());
                }))
                        .setCallback(s -> {
                            tp(s, true);
                        })
                        .addSubNode(argument("Worldname", dynamicList("Worldname", state -> {
                            return PlayerDAO.getPlayersWorldNames(state.getArgumentList().getString("Spielername"));
                        }))
                                .setCallback(s -> {
                                    tp(s, false);
                                }))));
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
        /* gs tp team */
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
        /* gs setTemplate <Template> */
        commandHandle.addSubNode(literal("setTemplate")
                .setRequirement(owner)
                .addSubNode(argument("template", dynamicList("template", s -> {
                    Set<String> out = new TreeSet<>();
                    PlayerDAO.getPlayersTeamplates(s.getPlayer().getUUID()).entrySet().forEach(entry -> {
                        if (entry.getValue()) {
                            out.add(entry.getKey().getName());
                        }
                    });
                    return out;
                }))
                        .setCallback(s -> {
                            setPlotTemplate(s, 1);
                        })
                        .addSubNode(invisible(argument("UUID1", dynamicList("UUID1", s -> {
                            return List.of(getPlayer(s.getArgumentList()).getUniqueId().toString());
                        }))
                                .setCallback(s -> {
                                    setPlotTemplate(s, 2);
                                })
                                .addSubNode(
                                        argument("UUID2", dynamicList("UUID2", s -> {
                                            return List.of(getPlayer(s.getArgumentList()).getUniqueId().toString());
                                        })).setCallback(s -> {
                                            setPlotTemplate(s, 3);
                                        }))))));
    }

    private void setPlotTemplate(ArgumentList s, int argsLength) {
        Player p = getPlayer(s);
        UUID uuid = p.getUniqueId();
        String templateName = s.getString("template");
        if (argsLength == 3 && p.getUniqueId().toString().equals(s.getString("UUID2"))) {
            Main.send(p, "gs_newTemplateGenerating", templateName);
            BauWorld bauWorld = WorldManager.get(p.getWorld());
            bauWorld.newWorld();
            bauWorld.setTemplate(s.getString("template"));
        } else if (argsLength == 2 && p.getUniqueId().toString().equals(s.getString("UUID1"))) {
            JsonCreater jsonMsg = new JsonCreater(Main.prefix + MessageHandler.getInstance().getString(p, "gs_newPlotTemplate_secondWarn", templateName));
            JsonCreater jsonMsgClick = new JsonCreater(MessageHandler.getInstance().getString(p, "gs_newPlotTemplate_secondWarn_click"));
            jsonMsgClick.addHoverEvent(MessageHandler.getInstance().getString(p, "gs_newPlotTemplate_secondWarn_clickHover", templateName));
            jsonMsg.addJson(jsonMsgClick.addClickEvent("/gs setTemplate " + templateName + " " + uuid.toString() + " " + uuid.toString(),
                    ClickAction.RUN_COMMAND)).send(p);
        } else if (argsLength == 1) {
            JsonCreater jsonMsg = new JsonCreater(Main.prefix + MessageHandler.getInstance().getString(p, "gs_newPlotTemplate_firstWarn", templateName));
            JsonCreater jsonMsgClick = new JsonCreater(MessageHandler.getInstance().getString(p, "gs_newPlotTemplate_firstWarn_click"));
            jsonMsgClick.addHoverEvent(MessageHandler.getInstance().getString(p, "gs_newPlotTemplate_firstWarn_clickHover", templateName));
            jsonMsg.addJson(jsonMsgClick.addClickEvent("/gs setTemplate " + templateName + " " + uuid.toString(), ClickAction.RUN_COMMAND)).send(p);
        }
    }

    private void tpTeam(ArgumentList s) {
        Player p = getPlayer(s);
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        Team team = TeamManager.getTeam(bauPlayer.getUuid());
        if (team == null) {
            MessageHandler.getInstance().send(p, "no_team");
        } else {
            if(team.isNewcomer(p.getUniqueId())){
                MessageHandler.getInstance().send(p, "team_newcomer");
            }else{
            BauWorld teamWorld = WorldManager.getTeamWorld(team);
            teamWorld.spawn(p);
            }
        }
    }

    private void rights(ArgumentList s, boolean b) {
        String memberName = s.getString("Mitglied");
        UUID memberUUID = DatabaseDAO.getUUID(memberName);
        Player p = getPlayer(s);

        EntityManagerExecuter.run(em -> {
            BauWorld bauWorld = WorldManager.get(p.getWorld());
            long id = bauWorld.getId();
            World dbWorld = em.find(World.class, id);
            net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class, memberUUID);
            WorldMember plotMember = dbWorld.getMember(dbPlayer);
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

    private void tp(ArgumentList s, boolean defaultWorld) {
        // /gs tp <WorldName>
        // /gs tp <SpielerName> <WorldName>
        // /gs tp team
        // /gs
        String name = s.getString("Spielername");
        String worldName = s.getString("Worldname");
        if (name != null && name.equalsIgnoreCase("team")) {
            tpTeam(s);
            return;
        }
        Player p = getPlayer(s);
        UUID ownerUUID = null;
        if (name == null) { // /gs
            ownerUUID = p.getUniqueId();
        } else {
            ownerUUID = DatabaseDAO.getUUID(name);
            if (ownerUUID == null) {
                return; //TODO error
            }

        }
        if (defaultWorld || worldName == null) {
            worldName = PlayerDAO.getDefaultWorldName(ownerUUID);
        }
        WorldManager.getPlayerWorld(worldName, ownerUUID).spawn(p);
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
        BauWorld world = WorldManager.getPlayerWorld(s.getString("Worlds"));
        if (world.newWorld()) {
            Main.send(getPlayer(s), "gsDeleted", s.getString("worlds"));
        }
    }


}
