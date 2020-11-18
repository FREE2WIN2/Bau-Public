package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Executor;
import net.wargearworld.GUI_API.GUI.ArgumentList;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.utils.CustomHeadValues;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.ArrayList;
import java.util.List;

public enum IconType {
    WORLD(true), MATERIAL(false);

    private List<WorldIcon> iconList;

    IconType(boolean head) {
        iconList = new ArrayList<>();
        EntityManagerExecuter.run(em->{
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Icon> cq = cb.createQuery(Icon.class);
            cq.from(Icon.class);
            List<Icon> icons = em.createQuery(cq).getResultList();
            for(Icon icon:icons){
                if(icon.getValue() != null && head){
                    iconList.add(new WorldIcon(icon));
                }else if(icon.getMaterial() != null && !head){
                    iconList.add(new WorldIcon(icon));
                }
            }
        });
    }

    public List<WorldIcon> getIconList() {
        return iconList;
    }

    public Item getItem(Executor<ArgumentList> executor, String name, List<String> lore) {
        switch (this) {
            case WORLD:
                return new HeadItem(new CustomHead(CustomHeadValues.GLOBE.getValue()),executor).setName(name).addLore(lore);
            case MATERIAL:
                return new DefaultItem(Material.REDSTONE,name,executor).addLore(lore);
        }
        return null;
    }
}
