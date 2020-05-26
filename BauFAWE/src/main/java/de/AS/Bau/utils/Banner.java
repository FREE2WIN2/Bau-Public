package de.AS.Bau.utils;


import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public enum Banner {

	ONE, TWO, THREE, FOUR, N, S, PLUS;

	public ItemStack create(DyeColor groundColor,DyeColor pictureColor , String name) {
		ItemStack banneris = new ItemStack(Material.WHITE_BANNER);
		BannerMeta bm = (BannerMeta) banneris.getItemMeta();
		
		/*set the groundcolor very cheaply...*/
		
		bm.addPattern(new Pattern(groundColor, PatternType.HALF_HORIZONTAL));
		bm.addPattern(new Pattern(groundColor, PatternType.HALF_HORIZONTAL_MIRROR));
		switch (this) {
		case ONE:
			bm.addPattern(new Pattern(pictureColor, PatternType.SQUARE_TOP_LEFT));
			bm.addPattern(new Pattern(groundColor, PatternType.TRIANGLES_TOP));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_CENTER));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_BOTTOM));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
			break;
		case TWO:
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_TOP));
			bm.addPattern(new Pattern(groundColor, PatternType.RHOMBUS_MIDDLE));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_BOTTOM));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_DOWNLEFT));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
			break;
		case THREE:
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_RIGHT));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_BOTTOM));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_TOP));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_MIDDLE));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
			break;
		case FOUR:
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_LEFT));
			bm.addPattern(new Pattern(groundColor, PatternType.HALF_HORIZONTAL_MIRROR));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_RIGHT));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_MIDDLE));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
			break;
		case N:
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_LEFT));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_DOWNRIGHT));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_RIGHT));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
			break;
		case S:
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_BOTTOM));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_TOP));
			bm.addPattern(new Pattern(groundColor, PatternType.RHOMBUS_MIDDLE));
			bm.addPattern(new Pattern(pictureColor, PatternType.STRIPE_DOWNRIGHT));
			bm.addPattern(new Pattern(groundColor, PatternType.CURLY_BORDER));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
			break;
		case PLUS:
			bm.addPattern(new Pattern(pictureColor, PatternType.STRAIGHT_CROSS));
			bm.addPattern(new Pattern(groundColor, PatternType.STRIPE_TOP));
			bm.addPattern(new Pattern(groundColor, PatternType.STRIPE_BOTTOM));
			bm.addPattern(new Pattern(groundColor, PatternType.BORDER));
		}
		bm.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		if(name!=null) {
			bm.setDisplayName(name);
		}
		banneris.setItemMeta(bm);
		return banneris;
	}
}