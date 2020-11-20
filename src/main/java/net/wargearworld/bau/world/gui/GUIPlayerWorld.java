package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.dao.PlotDAO;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.UUID;

public class GUIPlayerWorld implements IGUIWorld {
    private String name;
    private UUID owner;
    private String ownerName;
    private WorldIcon worldIcon;

    public GUIPlayerWorld(String name, UUID owner, String ownerName, WorldIcon worldIcon) {
        this.name = name;
        this.owner = owner;
        this.ownerName = ownerName;
        this.worldIcon = worldIcon;
    }

    public Item getIconItem(Player p) {
        if (owner != p.getUniqueId()) {
            return null;
        }
        return worldIcon.toItem().setName(MessageHandler.getInstance().getString(p, "world_gui_icon_change", name)).setExecutor(s -> {
            WorldGUI.openIcon(p, owner, name,IconType.WORLD);
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_icon_change_lore", name));
    }

    public Item getWorldItem(Player p) {
        return worldIcon.toItem().setName(MessageHandler.getInstance().getString(p, "world_gui_world_info", name)).setExecutor(s -> {
            WorldGUI.openPlayerWorldInfo(p, owner, name);
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_info_lore", name));
    }

    public Item getTeleportItem(Player p) {
        Item teleportItem = new DefaultItem(Material.ENDER_PEARL, MessageHandler.getInstance().getString(p, "world_gui_world_teleport", name), s -> {
            WorldManager.getPlayerWorld(name, owner).spawn(p);
            p.closeInventory();
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_teleport_lore", name));

        if(p.getWorld().getName().equalsIgnoreCase(owner.toString() + "_" + name)){
            teleportItem.addEnchantment(Enchantment.BINDING_CURSE,1);
            teleportItem.addItemFLags(ItemFlag.HIDE_ENCHANTS);
        }
        return teleportItem;
    }

    public Item getRenameItem(Player p) {
        if (owner != p.getUniqueId()) {
            return null;
        }
        return new DefaultItem(Material.NAME_TAG, MessageHandler.getInstance().getString(p, "world_gui_world_rename", name), s -> {
            WorldGUI.openRename(p, WorldManager.getPlayerWorld(name, owner));
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_rename_lore", name));
    }

    public Item getOwnerItem(Player p) {
        return new HeadItem(ownerName, MessageHandler.getInstance().getString(p, "world_gui_item_owner", ownerName), 1).setExecutor(s -> {
        });
    }

    @Override
    public Item getTimeIcon(Player p, World w, String worldName) {
        Item timeItem = new DefaultItem(Material.CLOCK, MessageHandler.getInstance().getString(p, "world_gui_item_time", w.getTime() + ""));
        if (owner.equals(p.getUniqueId())) {
            timeItem.addLore(MessageHandler.getInstance().getString(p, "world_gui_item_time_lore", w.getTime() + ""));
            timeItem.setExecutor(s -> {
                WorldGUI.openTimeChange(p, w,worldName);
            });
        }
        return timeItem;
    }

    @Override
    public Item getTemplateIcon(Player p) {
        WorldTemplate worldTemplate = PlotDAO.getTemplate(owner,name);
        if(worldTemplate == null)
            return null;
        Item item = worldTemplate.getItem(p.getUniqueId()).setName(MessageHandler.getInstance().getString(p, "world_gui_world_template", worldTemplate.getName()));
        if (p.getUniqueId().equals(owner) && p.getWorld().getName().equals(owner + "_" + name)) {
            item.setExecutor(s -> {
                WorldGUI.openTemplates(p, WorldManager.getPlayerWorld(name, owner));
            });
            item.addLore(MessageHandler.getInstance().getString(p, "world_gui_world_template_lore"));
        }
        return item;
    }

    @Override
    public Item getDefaultItem(Player p,int page) {
        boolean isOwner = p.getUniqueId().equals(owner);
        boolean isDefault = PlayerDAO.getDefaultWorldName(owner).equalsIgnoreCase(name);
        Item item = null;
        if(isDefault){
        item = new DefaultItem(Material.NETHER_STAR,MessageHandler.getInstance().getString(p,"world_gui_item_default"),s->{
        });
        }else if(isOwner) {
            item = new DefaultItem(Material.FIREWORK_STAR,MessageHandler.getInstance().getString(p,"world_gui_item_default_change"),s->{
                PlotDAO.changeToDefault(owner,name);
                WorldGUI.openMain(p,page);
            }).addLore(MessageHandler.getInstance().getString(p,"world_gui_item_default_change_lore",name));
        }
        return item;
    }
}
