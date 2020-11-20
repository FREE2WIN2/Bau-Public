package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.GUI.AnvilGUI;
import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.GUI.Slot;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.dao.PlotDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.team.TeamManager;
import net.wargearworld.bau.tools.cannon_timer.CannonTimerTick;
import net.wargearworld.bau.utils.CustomHeadValues;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.bau.world.bauworld.WorldMember;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.economy.core.account.Account;
import net.wargearworld.economy.core.utils.EconomyFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.ShortLookupTable;
import java.util.*;

public class WorldGUI implements Listener {

    public WorldGUI(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openMain(Player p, int page) {
        GUI mainGUI = new ChestGUI(54, MessageHandler.getInstance().getString(p, "worldGUI_title"));
        List<IGUIWorld> allWorlds = getAllWorlds(p);
        int maxPages = calcMaxPages(allWorlds);
        Collection<IGUIWorld> worlds = HelperMethods.getPage(allWorlds, page);
        if (worlds.size() == 0)
            openMain(p, --page);
        int currentColumn = 0;
        final int prevpage = page - 1;
        if (page > 1) {
            currentColumn++;
            Item prevpageItem = new HeadItem(new CustomHead(CustomHeadValues.ARROW_LEFT.getValue()), s -> {
                openMain(p, prevpage);
            }).setName(MessageHandler.getInstance().getString(p, "world_gui_previous_page", prevpage + ""));
            mainGUI.setItem(18, prevpageItem);
            mainGUI.setItem(35, prevpageItem);
        }
        for (IGUIWorld iguiWorld : worlds) {
            Item defaultItem = iguiWorld.getDefaultItem(p, page);
            if (defaultItem != null)
                mainGUI.setItem(currentColumn, defaultItem);

            Item ownerItem = iguiWorld.getOwnerItem(p);
            if (ownerItem != null)
                mainGUI.setItem(currentColumn + 9, ownerItem);

            Item worldItem = iguiWorld.getWorldItem(p);
            if (worldItem != null)
                mainGUI.setItem(currentColumn + 18, worldItem);

            Item templateIcon = iguiWorld.getTemplateIcon(p);
            if (templateIcon != null)
                mainGUI.setItem(currentColumn + 27, templateIcon);

            Item teleportItem = iguiWorld.getTeleportItem(p);
            if (teleportItem != null) {
                mainGUI.setItem(currentColumn + 36, teleportItem);
            }

            Item renameItem = iguiWorld.getRenameItem(p);
            if (renameItem != null)
                mainGUI.setItem(currentColumn + 45, iguiWorld.getRenameItem(p));
            currentColumn++;
        }
        final int nextpage = page + 1;
        if (page < maxPages) {
            Item nextPage = new HeadItem(new CustomHead(CustomHeadValues.ARROW_RIGHT.getValue()), s -> {
                openMain(p, nextpage);
            }).setName(MessageHandler.getInstance().getString(p, "world_gui_next_page", nextpage + ""));
            mainGUI.setItem(26, nextPage);
            mainGUI.setItem(35, nextPage);
        }


        mainGUI.open(p);
    }

    private static int calcMaxPages(List<IGUIWorld> allWorlds) {
        int size = allWorlds.size();
        if (size <= 8)
            return 1;
        return (size + 6) / 7;
    }

    private static List<IGUIWorld> getAllWorlds(Player p) {
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        return EntityManagerExecuter.run(em -> {
            List<IGUIWorld> worlds = new ArrayList<>();
            //First Own
            for (Plot plot : bauPlayer.getdbPlots()) {
                worlds.add(new GUIPlayerWorld(plot.getName(), plot.getOwner().getUuid(), plot.getOwner().getName(), new WorldIcon(plot.getIcon())));
            }
            for (int i = worlds.size(); i < BauConfig.getInstance().getMaxworlds(); i++) {
                worlds.add(new GUIMissingPlayerWorld());
            }
            Team team = TeamManager.getTeam(p.getUniqueId());
            if (team != null) {
                worlds.add(new GUITeamWorld(team));
            } else {
                worlds.add(new GUIMissingTeamWorld());
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
                        if (!p.getWorld().equals(bauWorld.getWorld())) {
                            MessageHandler.getInstance().send(p, "world_template_not_on_world", bauWorld.getName(), worldTemplate.getName());
                        } else {
                            p.performCommand("gs setTemplate " + worldTemplate.getName());
                        }
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
        openWorldInfo(p, WorldManager.getPlayerWorld(worldName, owner), true);
    }

    public static void openTeamWorldInfo(Player p, Team team) {
        if (team != null)
            openWorldInfo(p, WorldManager.getTeamWorld(team), true);
    }

    public static void openWorldInfo(Player p, BauWorld bauWorld, boolean openMainIfClosed) {
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
            }).setCancelled(true);
            if (bauWorld.isOwner(p)) {
                memberHead.setExecutor(s -> {
                    bauWorld.removeMember(member.getUuid());
                    p.getOpenInventory().getTopInventory().setItem(s.getClickedIndex(), new ItemStack(Material.AIR));
                    p.updateInventory();
                }).addLore(msgHandler.getString(p, "world_gui_member_lore", member.getName()));
            }
            memberHead.addLore(msgHandler.getString(p,"world_gui_member_rights_lore_" + member.hasRights()));
            gui.addItem(memberHead);
        }

        IGUIWorld guiWorld = bauWorld.getGUIWorld();
        List<Item> items = new ArrayList<>();
        items.add(guiWorld.getOwnerItem(p));
        items.add(guiWorld.getRenameItem(p));
        if (p.getWorld().getUID().equals(bauWorld.getWorldUUID())) {
            items.add(guiWorld.getTemplateIcon(p));
        } else {
            items.add(guiWorld.getTeleportItem(p));
        }
        items.add(guiWorld.getIconItem(p));//Icon
        items.add(guiWorld.getTimeIcon(p, bauWorld.getWorld(), bauWorld.getName()));
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
        if (openMainIfClosed)
            gui.onClose(s -> {
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {

                    openMain(p, 1);
                }, 1);
            });
        gui.open(p);
    }

    public static void openIcon(Player p, UUID owner, String worldName, IconType iconType) {
        List<WorldIcon> icons = iconType.getIconList();
        int size = ((icons.size() + 8) / 9) * 9 + 9;
        if (size > 54) {
            icons = icons.subList(0, 45);
            size = 54;
        }
        GUI gui = new ChestGUI(size, MessageHandler.getInstance().getString(p, "world_choose_icon_gui", worldName));
        for (WorldIcon worldIcon : icons) {
            gui.addItem(worldIcon.toItem().setExecutor(s -> {
                PlotDAO.setIcon(owner, worldName, worldIcon.getId());
                openMain(p, 1);
            }));
        }
        gui.setItem(size - 6, IconType.WORLD.getItem(s -> {
            openIcon(p, owner, worldName, IconType.WORLD);
        }, MessageHandler.getInstance().getString(p, "world_choose_icon_world"), new ArrayList<>()));
        gui.setItem(size - 4, IconType.MATERIAL.getItem(s -> {
            openIcon(p, owner, worldName, IconType.MATERIAL);
        }, MessageHandler.getInstance().getString(p, "world_choose_icon_material"), new ArrayList<>()));
        gui.open(p);
    }

    public static void openRename(Player p, BauWorld bauWorld) {
        if (bauWorld instanceof TeamWorld)
            return;

        String oldName = bauWorld.getName();
        AnvilGUI anvilGUI = new AnvilGUI(MessageHandler.getInstance().getString(p, "world_rename_world_gui", oldName), oldName, s -> {
            ItemStack is = s.getClicked();
            if (is == null || !is.hasItemMeta())
                return;
            String newName = s.getClicked().getItemMeta().getDisplayName();
            MessageHandler.getInstance().send(p, "world_renamed", oldName, newName);
            p.closeInventory();
            WorldManager.renameWorld(bauWorld, newName);
        });
        anvilGUI.setItem(Slot.INPUT_LEFT, bauWorld.getGUIWorld().getIconItem(p).setExecutor(s -> {
        }).setLore(new ArrayList<>()).setName(oldName));
        anvilGUI.open(p);
    }

    public static void openTimeChange(Player p, World w, String worldName) {
        AnvilGUI gui = new AnvilGUI(MessageHandler.getInstance().getString(p, "world_world_change_time_gui", worldName), w.getTime() + "", s -> {
            ItemStack is = s.getClicked();
            if (is == null || !is.hasItemMeta())
                return;
            String value = s.getClicked().getItemMeta().getDisplayName();
            value = value.replace(" ", "");

            if (!HelperMethods.isInt(value)) {
                MessageHandler.getInstance().send(p, "no_integer", value);
            } else {
                w.setTime(Integer.parseInt(value));
                MessageHandler.getInstance().send(p, "world_time_setted", worldName);
            }
            p.closeInventory();
        });
        gui.open(p);
    }

    public static void openBuyWorldName(Player p) {
        AnvilGUI gui = new AnvilGUI(MessageHandler.getInstance().getString(p, "world_buy_world_name_gui"), " ", s -> {
            ItemStack is = s.getClicked();
            if (is == null || !is.hasItemMeta())
                return;
            String worldName = s.getClicked().getItemMeta().getDisplayName();
            if (worldName.startsWith(" "))
                worldName = worldName.replaceFirst(" ", "");
            s.getPlayer().performCommand("buy world " + worldName);
            s.getPlayer().closeInventory();
        });
        gui.open(p);
    }
}
