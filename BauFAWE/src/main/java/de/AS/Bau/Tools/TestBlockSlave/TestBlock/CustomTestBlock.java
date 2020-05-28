package de.AS.Bau.Tools.TestBlockSlave.TestBlock;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.WorldEdit.Schematic;
import de.AS.Bau.utils.Banner;
import de.AS.Bau.utils.Facing;

public class CustomTestBlock implements TestBlock {

	/*
	 * This is a TestBlock out of the personal .yml of the Player.
	 * 
	 */

	private String owner;
	private Schematic schematic;
	private String Name;
	private int tier;
	private boolean favorite;
	/**
	 * Creates a new TestBlock
	 * 
	 * @param owner     -> UUID of the owner. If the TestBlock is a default one
	 *                  owner is not an empty String!
	 * @param schemName -> name of the schemfile
	 * @param Name      -> Name of the TestBlock
	 * @param tier      -> int-value of the tier
	 */
	public CustomTestBlock(String owner, String schemName, String Name, String face, int tier, boolean favorite) {
		this.owner = owner;
		this.schematic = new Schematic(owner + "/TestBlockSklave", schemName, Facing.getByShort(face.toUpperCase()));
		this.Name = Name;
		this.tier = tier;
		this.favorite = favorite;
	}
	public CustomTestBlock(Player owner, String name, Facing facing, int tier) {
		this.owner = owner.getUniqueId().toString();
		this.Name = name;
		this.tier = tier;
		this.schematic = new Schematic(this.owner + "/TestBlockSklave", name + ".schem", facing);
		this.favorite = false;
	}

 	public int getTier() {
		return tier;
	}

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return Name;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean fav) {
		favorite = fav;
	}
	
	@Override
	public Schematic getSchematic() {
		return schematic;
	}

	public ItemStack getBanner() {
		DyeColor groundColor;
		if (favorite) {
			groundColor = DyeColor.LIME;
		} else {
			groundColor = DyeColor.ORANGE;
		}
		switch (tier) {
		case 1:
			return Banner.ONE.create(groundColor, DyeColor.BLACK, Name);
		case 2:
			return Banner.TWO.create(groundColor, DyeColor.BLACK, Name);
		case 3:
			return Banner.THREE.create(groundColor, DyeColor.BLACK, Name);
		}
		return null;
	}
}
