package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.Item;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface IGUIWorld {
    public Item getIconItem(Player p);
    public Item getWorldItem(Player p);
    public Item getTeleportItem(Player p);
    public Item getRenameItem(Player p);
    public Item getOwnerItem(Player p);
    public Item getTimeIcon(Player p, World w);
    public Item getTemplateIcon(Player p);
    public Item getDefaultItem(Player p,int page);
}
