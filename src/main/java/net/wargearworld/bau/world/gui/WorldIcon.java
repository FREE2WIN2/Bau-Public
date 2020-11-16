package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.db.model.Icon;
import org.bukkit.Material;

public class WorldIcon {
    private static final String GLOBE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWMxMWQ2Yzc5YjhhMWYxODkwMmQ3ODNjZGRhNGJkZmI5ZDQ3MzM3YjczNzkxMDI4YTEyNmE2ZTZjZjEwMWRlZiJ9fX0=";

    private String value;
    private String materialName;

    public WorldIcon(String value, String materialName) {
        this.value = value;
        this.materialName = materialName;
    }

    public WorldIcon(Icon icon) {
        this.value = icon.getValue();
        this.materialName = icon.getMaterial();
    }

    public Item toItem(){
        if(value !=null){
            return new HeadItem(new CustomHead(value),s->{});
        }else if(materialName != null){
            return new DefaultItem(Material.valueOf(materialName.toUpperCase()), s->{});
        }else{
            return new HeadItem(new CustomHead(GLOBE),s->{});
        }
    }
}
