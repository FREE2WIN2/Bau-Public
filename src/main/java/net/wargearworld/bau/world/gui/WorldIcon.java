package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.utils.CustomHeadValues;
import net.wargearworld.db.model.Icon;
import org.bukkit.Material;

public class WorldIcon {

    private String value;
    private String materialName;
    private Long id;
    private String name;
    public WorldIcon(Icon icon) {
        if(icon == null)
            return;
        this.value = icon.getValue();
        this.materialName = icon.getMaterial();
        this.id = icon.getId();
        this.name = icon.getName();
    }

    public Item toItem(){
        Item item;
        if(value !=null){
            item = new HeadItem(new CustomHead(value),s->{});
        }else if(materialName != null){
           item = new DefaultItem(Material.valueOf(materialName.toUpperCase()), s->{});
        }else{
            item = new HeadItem(new CustomHead(CustomHeadValues.GLOBE.getValue()), s->{});
        }
        if(name !=null)
            item.setName("§6" + name);
        return item;
    }

    public Long getId() {
        return id;
    }
}
