package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.dao.PlotDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Plot;
import net.wargearworld.economy.core.utils.EconomyFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class WorldGUI implements Listener {

    private static final String TEMPLATE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWMxMWQ2Yzc5YjhhMWYxODkwMmQ3ODNjZGRhNGJkZmI5ZDQ3MzM3YjczNzkxMDI4YTEyNmE2ZTZjZjEwMWRlZiJ9fX0=";

    public WorldGUI(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openMain(Player p, int page) {
        GUI mainGUI = new ChestGUI(36, MessageHandler.getInstance().getString(p, "worldGUI_title"));
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        EntityManagerExecuter.run(em -> {
            for (Plot plot : bauPlayer.getdbPlots()) {
//               Item worldItem = new HeadItem(new CustomHead(GLOBE),s->{
//                   s.getPlayer().performCommand("")
//               })
            }
        });
        Item item = new HeadItem(new CustomHead(TEMPLATE), s -> {
            openTemplates(s.getPlayer());
        });
        mainGUI.setItem(2, item);
        mainGUI.open(p);
    }

    public static void openTemplates(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        BauWorld bauWorld = WorldManager.get(p.getWorld());
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
                        s.getPlayer().performCommand("gs setTemplate " + worldTemplate.getName());
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
                    if (bauWorld.isOwner(s.getPlayer())) {
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
        MessageHandler msgHandler = MessageHandler.getInstance();
        if (bauWorld == null) {
            //TODO Message
            return;
        }
        boolean isLeader = bauWorld.isOwner(p);
        Collection<String> members = bauWorld.getMemberNames();
        int size = (members.size() + 8) / 9 + 18;
        if (size > 54) size = 54;
        GUI gui;
        String worldName = bauWorld.getName();
        if (bauWorld instanceof PlayerWorld) {
            gui = new ChestGUI(size, msgHandler.getString(p, "world_gui_title_player", bauWorld.getName()));
        } else {
            gui = new ChestGUI(size, msgHandler.getString(p, "world_gui_title_team", bauWorld.getName()));
        }
        for (String member : members) {
            HeadItem memberHead = new HeadItem(member, "§3" + member, 1);
            memberHead.setExecutor(s -> {
                bauWorld.removeMember(DatabaseDAO.getUUID(member));
            }).addLore(msgHandler.getString(p, "world_gui_member_lore", member));
            gui.addItem(memberHead);
        }
        String ownerName = DatabaseDAO.getPlayer(UUID.fromString(bauWorld.getOwner())).getName();

        Item ownerItem = new HeadItem(ownerName,msgHandler.getString(p, "world_gui_item_owner",ownerName),1);
        Item renameItem =  new DefaultItem(Material.NAME_TAG, MessageHandler.getInstance().getString(p, "world_gui_world_rename", worldName), s -> {
            WorldGUI.openRename(p, bauWorld);
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_rename_lore", worldName));

    }

    public static void openIcon(Player p, UUID owner, String worldName) {
    }

    public static void openRename(Player p, BauWorld bauWorld) {
    }
}
