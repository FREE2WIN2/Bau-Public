package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.world.WorldManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
            return new DefaultItem(Material.AIR);
        }
        return new DefaultItem(Material.NAME_TAG, MessageHandler.getInstance().getString(p, "world_gui_icon_rename", name), s -> {
            WorldGUI.openIcon(p, owner, name);
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_icon_lore", name));
    }

    public Item getWorldItem(Player p) {
        //TODO if no Icon -> TemplateIcon
        return worldIcon.toItem().setName(MessageHandler.getInstance().getString(p, "world_gui_world_info", name)).setExecutor(s -> {
            WorldGUI.openPlayerWorldInfo(p, owner, name);
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_info_lore", name));
    }

    public Item getTeleportItem(Player p) {
        return new DefaultItem(Material.ENDER_PEARL, MessageHandler.getInstance().getString(p, "world_gui_world_teleport", name), s -> {
            p.performCommand("/gs tp " + ownerName + " " + name);
            p.closeInventory();
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_teleport_lore", name));
    }

    public Item getRenameItem(Player p) {
        if (owner != p.getUniqueId()) {
            return new DefaultItem(Material.AIR);
        }
        return new DefaultItem(Material.NAME_TAG, MessageHandler.getInstance().getString(p, "world_gui_world_rename", name), s -> {
            WorldGUI.openRename(p, WorldManager.getPlayerWorld(name, owner));
        }).addLore(MessageHandler.getInstance().getString(p, "world_gui_world_rename_lore", name));
    }

    public Item getOwnerItem(Player p) {
        return new HeadItem(ownerName,MessageHandler.getInstance().getString(p, "world_gui_item_owner", ownerName),1).setExecutor(s->{});
    }
}
