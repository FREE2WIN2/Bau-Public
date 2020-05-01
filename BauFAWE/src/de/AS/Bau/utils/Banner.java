package de.AS.Bau.utils;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public enum Banner {

	ONE("1", Material.WHITE_BANNER), TWO("2", Material.WHITE_BANNER), THREE("3", Material.WHITE_BANNER),
	FOUR("4", Material.WHITE_BANNER),N("N",Material.WHITE_BANNER),S("S",Material.WHITE_BANNER);

	private ItemStack is;
	Banner(String b, Material g) {
		is = create(b,g);
	}

	private ItemStack create(String bannerName, Material groundMaterial) {
			ItemStack banneris= new ItemStack(groundMaterial);
			BannerMeta bm = (BannerMeta) banneris.getItemMeta();
			if (bannerName.equals("1")) {
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.SQUARE_TOP_LEFT));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.TRIANGLES_TOP));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.BORDER));
			}
			else if (bannerName.equals("2")) {
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.BORDER));
			}
				else if (bannerName.equals("3")) {
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.BORDER));
			}else if(bannerName.equals("4")){
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL_MIRROR));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.BORDER));
			}else if(bannerName.equals("N")) {
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.BORDER));
			}else if(bannerName.equals("S")) {
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
				bm.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER));
				bm.addPattern(new Pattern(DyeColor.WHITE, PatternType.BORDER));
			}
			bm.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			//bm.setLore(new ArrayList<String>());
			banneris.setItemMeta(bm);
			return banneris;
			}
	public ItemStack getItemStack(){
		return is;
	}
	
	public ItemStack setName(String displayName) {
		BannerMeta bm = (BannerMeta) is.getItemMeta();
		bm.setDisplayName(displayName);
		is.setItemMeta(bm);
		return is;
	}
}