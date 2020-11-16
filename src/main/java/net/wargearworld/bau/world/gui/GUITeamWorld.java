package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.Item;
import org.bukkit.entity.Player;

public class GUITeamWorld implements IGUIWorld{
    private static final String TEAMWORLD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThkYWExZTNlZDk0ZmYzZTMzZTFkNGM2ZTQzZjAyNGM0N2Q3OGE1N2JhNGQzOGU3NWU3YzkyNjQxMDYifX19";

    @Override
    public Item getIconItem(Player p) {
        return null;
    }

    @Override
    public Item getWorldItem(Player p) {
        return null;
    }

    @Override
    public Item getTeleportItem(Player p) {
        return null;
    }

    @Override
    public Item getRenameItem(Player p) {
        return null;
    }

    @Override
    public Item getOwnerItem(Player p) {
        return getIconItem(p);
    }
}
