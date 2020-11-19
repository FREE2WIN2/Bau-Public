package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlotDAO;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.db.EntityManagerExecuter;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class GUITeamWorld implements IGUIWorld{
    private static final String TEAMWORLD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThkYWExZTNlZDk0ZmYzZTMzZTFkNGM2ZTQzZjAyNGM0N2Q3OGE1N2JhNGQzOGU3NWU3YzkyNjQxMDYifX19";

    private Team team;

    public GUITeamWorld(Team team) {
        this.team = team;
    }

    @Override
    public Item getIconItem(Player p) {
        return getWorldItem(p);
    }

    @Override
    public Item getWorldItem(Player p) {
        return new HeadItem(new CustomHead(TEAMWORLD),s -> {
            WorldGUI.openTeamWorldInfo(p, team);
        }).setName(MessageHandler.getInstance().getString(p, "world_gui_world_info", team.getName())).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_info_lore", team.getName()));
    }

    @Override
    public Item getTeleportItem(Player p) {
        return new DefaultItem(Material.ENDER_PEARL, MessageHandler.getInstance().getString(p, "world_gui_world_teleport", "Team: " + team.getName()), s -> {
            p.performCommand("/gs team");
            p.closeInventory();
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_teleport_lore", "Team: " + team.getName()));
    }

    @Override
    public Item getRenameItem(Player p) {
        return null;
    }

    @Override
    public Item getOwnerItem(Player p) {
        ItemStack bannerItem = new ItemStack(Material.GRAY_BANNER);
        BannerMeta bannerMeta = (BannerMeta) bannerItem.getItemMeta();
        bannerMeta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.STRIPE_TOP));
        bannerMeta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.STRIPE_BOTTOM));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.RHOMBUS_MIDDLE));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.CURLY_BORDER));
        bannerMeta.addPattern(new Pattern(DyeColor.YELLOW, PatternType.GLOBE));
        bannerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_UNBREAKABLE);
        bannerItem.setItemMeta(bannerMeta);
        Item item = new DefaultItem(bannerItem,s->{}).setName(MessageHandler.getInstance().getString(p,"world_gui_item_owner", "Team: " + team.getName()));
        return item;
    }

    @Override
    public Item getTimeIcon(Player p, World w, String worldName) {
        Item timeItem = new DefaultItem(Material.CLOCK, MessageHandler.getInstance().getString(p, "world_gui_item_time", w.getTime() + ""));
        if (team.isLeader(p.getUniqueId())) {
            timeItem.addLore(MessageHandler.getInstance().getString(p, "world_gui_item_time_lore", w.getTime() + ""));
            timeItem.setExecutor(s -> {
                WorldGUI.openTimeChange(p, w,worldName);
            });
        }
        return timeItem;
    }

    @Override
    public Item getTemplateIcon(Player p) {
        WorldTemplate worldTemplate = team.getTemplate();
        Item item = worldTemplate.getItem(p.getUniqueId()).setName(MessageHandler.getInstance().getString(p, "world_gui_world_template", worldTemplate.getName()));
        if (team.isLeader(p.getUniqueId())&& p.getWorld().getName().equals("team_" + team.getId())) {
            item.setExecutor(s -> {
                WorldGUI.openTemplates(p, WorldManager.getTeamWorld(team));
            });
            item.addLore(MessageHandler.getInstance().getString(p, "world_gui_world_template_lore"));
        }
        return item;
    }

    @Override
    public Item getDefaultItem(Player p,int page) {
        return null;
    }
}
