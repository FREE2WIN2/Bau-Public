package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.CustomHeadValues;
import net.wargearworld.db.EntityManagerExecuter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class GUIMissingPlayerWorld implements IGUIWorld{
    @Override
    public Item getIconItem(Player p) {
        return null;
    }

    @Override
    public Item getWorldItem(Player p) {
        int amountOfWorlds = EntityManagerExecuter.run(em->{return BauPlayer.getBauPlayer(p).getdbWorlds().size();});
        Double price = amountOfWorlds * BauConfig.getInstance().getWorldprice();
       Item item = new HeadItem(new CustomHead(CustomHeadValues.PLUS.getValue()),s->{
            p.performCommand("buy world");
        }).addItemFLags(ItemFlag.values()).addLore(MessageHandler.getInstance().getString(p,"world_gui_item_addworld_lore",price + "")).setName(MessageHandler.getInstance().getString(p,"world_gui_item_addworld"));
       return item;
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
        return null;
    }

    @Override
    public Item getTimeIcon(Player p, World w, String worldName) {
        return null;
    }

    @Override
    public Item getTemplateIcon(Player p) {
        return null;
    }

    @Override
    public Item getDefaultItem(Player p,int page) {
        return null;
    }
}
