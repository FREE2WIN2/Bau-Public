package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.Item;
import org.bukkit.entity.Player;

public interface IGUIWorld {
    public Item getIconItem(Player p);
    public Item getWorldItem(Player p);
    public Item getTeleportItem(Player p);
    public Item getRenameItem(Player p);
    public Item getOwnerItem(Player p);
}
