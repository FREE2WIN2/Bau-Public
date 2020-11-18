package net.wargearworld.bau.world.gui;

import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIMissingTeamWorld implements IGUIWorld{
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
        ItemStack bannerItem = new ItemStack(Material.GRAY_BANNER);
        BannerMeta bannerMeta = (BannerMeta) bannerItem.getItemMeta();
        bannerMeta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.STRIPE_TOP));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.RHOMBUS_MIDDLE));
        bannerMeta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.STRIPE_DOWNLEFT));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.HALF_HORIZONTAL_MIRROR));
        bannerMeta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.TRIANGLE_BOTTOM));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.STRIPE_MIDDLE));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.STRIPE_BOTTOM));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.BORDER));
        bannerMeta.addPattern(new Pattern(DyeColor.GRAY, PatternType.CURLY_BORDER));
//        bannerMeta.addPattern(new Pattern(DyeColor.YELLOW, PatternType.GRADIENT_UP));
        bannerMeta.addItemFlags(ItemFlag.values());
        bannerMeta.setDisplayName(MessageHandler.getInstance().getString(p,"world_gui_item_noTeam"));
        bannerMeta.setLore(new ArrayList<>());
        bannerMeta.setLore(List.of(MessageHandler.getInstance().getString(p,"world_gui_item_noTeam_lore")));
        bannerItem.setItemMeta(bannerMeta);
        Item item = new DefaultItem(bannerItem,s->{});
        return item;
    }

    @Override
    public Item getTimeIcon(Player p, World w) {
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
