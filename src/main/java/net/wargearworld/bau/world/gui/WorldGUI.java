package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.dao.PlotDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.team.TeamManager;
import net.wargearworld.bau.tools.cannon_timer.CannonTimerTick;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.WorldMember;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.economy.core.utils.EconomyFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class WorldGUI implements Listener {

    private static final String TEMPLATE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWMxMWQ2Yzc5YjhhMWYxODkwMmQ3ODNjZGRhNGJkZmI5ZDQ3MzM3YjczNzkxMDI4YTEyNmE2ZTZjZjEwMWRlZiJ9fX0=";
    private static final String GREEN_PLUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19";
    private static final String ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ==";
    private static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19";

    public WorldGUI(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openMain(Player p, int page) {
        GUI mainGUI = new ChestGUI(45, MessageHandler.getInstance().getString(p, "worldGUI_title"));
        List<IGUIWorld> allWorlds = getAllWorlds(p);
        int maxPages = calcMaxPages(allWorlds);
        Collection<IGUIWorld> worlds = getWorlds(allWorlds, page);
        if (worlds.size() == 0)
            openMain(p, --page);
        int currentColumn = 0;
        final int prevpage = page - 1;
        if(page > 1){
            currentColumn++;
            Item prevpageItem = new HeadItem(new CustomHead(ARROW_LEFT), s -> {
                openMain(p, prevpage);
            }).setName(MessageHandler.getInstance().getString(p, "world_gui_previous_page", prevpage + ""));
            mainGUI.setItem(18, prevpageItem);
        }
        for (IGUIWorld iguiWorld : worlds) {
            Item ownerItem = iguiWorld.getOwnerItem(p);
            if (ownerItem != null)
                mainGUI.setItem(currentColumn, ownerItem);

            mainGUI.setItem(currentColumn + 9, iguiWorld.getWorldItem(p));
            mainGUI.setItem(currentColumn + 18, iguiWorld.getTemplateIcon(p));
            mainGUI.setItem(currentColumn + 27, iguiWorld.getTeleportItem(p));

            Item renameItem = iguiWorld.getRenameItem(p);
            if (renameItem != null)
                mainGUI.setItem(currentColumn + 36, iguiWorld.getRenameItem(p));
            currentColumn++;
        }
        final int nextpage = page + 1;
        if (page < maxPages) {
            Item nextPage = new HeadItem(new CustomHead(ARROW_RIGHT), s -> {
                openMain(p, nextpage);
            }).setName(MessageHandler.getInstance().getString(p, "world_gui_next_page", nextpage + ""));
            mainGUI.setItem(26, nextPage);
        }


        mainGUI.open(p);
    }

    private static int calcMaxPages(List<IGUIWorld> allWorlds) {
        int size = allWorlds.size();
        if (size <= 9)
            return 1;
        return (size + 7) / 8;
    }

    private static Collection<IGUIWorld> getWorlds(List<IGUIWorld> worldsOrigin, int page) {
        List<IGUIWorld> worlds = new ArrayList<>(worldsOrigin);
        int size = worlds.size();
        int begin = 0;
        int pageSize = 8;
        if (page == 1) {
            if (worlds.size() <= 9) {
                return worlds;
            }
        } else {
            begin += 8;
            begin += (page - 2) * 7;
            pageSize = 7;
        }
        if (pageSize >= size) {
            return worlds;
        }
        if (size - 1 < begin) {
            return worlds;
        }
        if (size - 1 < begin + pageSize) {
            worlds = worlds.subList(begin, size);
        } else {
            worlds = worlds.subList(begin, begin + pageSize);
        }
        return worlds;
    }

    private static List<IGUIWorld> getAllWorlds(Player p) {
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        return EntityManagerExecuter.run(em -> {
            List<IGUIWorld> worlds = new ArrayList<>();
            //First Own
            for (Plot plot : bauPlayer.getdbPlots()) {
                worlds.add(new GUIPlayerWorld(plot.getName(), plot.getOwner().getUuid(), plot.getOwner().getName(), new WorldIcon(plot.getIcon())));
            }
            //then team
            Team team = TeamManager.getTeam(p.getUniqueId());
            if (team != null) {
                worlds.add(new GUITeamWorld(team));
            }
            //Then membered
            for (PlotMember plotMember : PlayerDAO.getMemberedPlots(p.getUniqueId())) {
                Plot plot = plotMember.getPlot();
                worlds.add(new GUIPlayerWorld(plot.getName(), plot.getOwner().getUuid(), plot.getOwner().getName(), new WorldIcon(plot.getIcon())));
            }
            return worlds;
        });

    }

    public static void openTemplates(Player p, BauWorld bauWorld) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        Map<WorldTemplate, Boolean> playersTemplates = PlayerDAO.getPlayersTeamplates(bauPlayer.getUuid());
        int invSize = (playersTemplates.size() + 8) / 9;
        GUI gui = new ChestGUI(invSize * 9, MessageHandler.getInstance().getString(p, "worldTemplateGUI_title"));
        EconomyFormatter economyFormatter = EconomyFormatter.getInstance();
        for (Map.Entry<WorldTemplate, Boolean> entry : playersTemplates.entrySet()) {
            WorldTemplate worldTemplate = entry.getKey();
            Item worldTemplateItem = entry.getKey().getItem(bauPlayer.getUuid());
            String prefix = "§c";

            worldTemplateItem.addLore("§8§m                    ");
            worldTemplateItem.addLore(" ");
            if (entry.getValue() || worldTemplate.getPrice() == 0) {
                if (!entry.getValue()) {
                    PlayerDAO.addPlotTemplate(worldTemplate, p.getUniqueId());
                }
                prefix = "§3";
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_available_lore"));
                worldTemplateItem.setExecutor(s -> {
                    if (bauWorld.isOwner(s.getPlayer())) {
                        s.getPlayer().closeInventory();
                        bauWorld.setTemplate(worldTemplate);
                        MessageHandler.getInstance().send(p, "world_template_setted", bauWorld.getName(), worldTemplate.getName());
                    }
                });
            } else {
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_not_available_lore"));
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_price_lore", economyFormatter.format(worldTemplate.getPrice())));
                worldTemplateItem.setExecutor(s -> {
                    p.performCommand("buy template " + worldTemplate.getName());
                    p.getOpenInventory().close();
                });
            }
            if (bauWorld.getTemplate().equals(entry.getKey())) {
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_active_lore"));
                worldTemplateItem.setExecutor(s -> {
                    if (bauWorld.isOwner(s.getPlayer()) && p.getWorld().getUID().equals(bauWorld.getWorldUUID())) {
                        s.getPlayer().performCommand("gs new");
                        s.getPlayer().closeInventory();
                    }
                });
            }
            //ODO addLore
            worldTemplateItem.setName(prefix + entry.getKey().getName());


            gui.addItem(worldTemplateItem);
        }
        gui.open(p);
    }

    public static void openPlayerWorldInfo(Player p, UUID owner, String worldName) {
        openWorldInfo(p, WorldManager.getPlayerWorld(worldName, owner));
    }

    public static void openTeamWorldInfo(Player p, Team team) {
        if (team != null)
            openWorldInfo(p, WorldManager.getTeamWorld(team));
    }

    public static void openWorldInfo(Player p, BauWorld bauWorld) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            MessageHandler msgHandler = MessageHandler.getInstance();
            if (bauWorld == null) {
                //TODO Message
                return;
            }
            Collection<WorldMember> members = bauWorld.getMembers();
            int size = ((members.size() + 8) / 9) * 9 + 18;
            if (size > 54) size = 54;
            GUI gui;
            if (bauWorld instanceof PlayerWorld) {
                gui = new ChestGUI(size, msgHandler.getString(p, "world_gui_title_player", bauWorld.getName()));
            } else {
                gui = new ChestGUI(size, msgHandler.getString(p, "world_gui_title_team", bauWorld.getName()));
            }
            for (WorldMember member : members) {
                HeadItem memberHead = new HeadItem(member.getUuid(), "§3" + member.getName(), 1);
                memberHead.setExecutor(s -> {
                    bauWorld.removeMember(member.getUuid());
                    p.getOpenInventory().getTopInventory().setItem(s.getClickedIndex(), new ItemStack(Material.AIR));
                    p.updateInventory();
                }).addLore(msgHandler.getString(p, "world_gui_member_lore", member.getName()));
                gui.addItem(memberHead);
            }

            IGUIWorld guiWorld = bauWorld.getGUIWorld();
            List<Item> items = new ArrayList<>();
            items.add(guiWorld.getOwnerItem(p));
            items.add(guiWorld.getRenameItem(p));
            items.add(guiWorld.getTeleportItem(p));
            items.add(guiWorld.getIconItem(p));//Icon
            items.add(guiWorld.getTimeIcon(p, bauWorld.getWorld()));
            items.remove(null);
            Iterator<Integer> iterator = HelperMethods.getMiddlePositions(items.size()).iterator();
            Iterator<Item> itemIterator = items.iterator();
            while (iterator.hasNext() && itemIterator.hasNext()) {
                Item item = itemIterator.next();
                if (item == null) {
                    iterator.next();
                    continue;
                }
                gui.setItem(size - (9 - iterator.next()), item);
                item.build();
            }
            Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                gui.open(p);
            });
        });
    }

    public static void openIcon(Player p, UUID owner, String worldName) {
    }

    public static void openRename(Player p, BauWorld bauWorld) {
    }

    public static void openTimeChange(Player p, World w) {
    }
}
