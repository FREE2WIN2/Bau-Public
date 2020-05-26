package de.AS.Bau.Tools.TestBlockSlave;

import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.WorldEdit.Schematic;
import de.AS.Bau.utils.Banner;

public class TestBlock {

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
	 *                  owner is an empty String!
	 * @param schemName -> name of the schemfile
	 * @param Name      -> Name of the TestBlock
	 * @param tier      -> int-value of the tier
	 */
	public TestBlock(String owner, String schemName, String Name, int tier, boolean favorite) {
		this.owner = owner;
		this.schematic = new Schematic(owner + "/TestBlockSklave", schemName);
		this.Name = Name;
		this.tier = tier;
		this.favorite = favorite;
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

	public Schematic getSchem() {
		return schematic;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public ItemStack getBanner() {
		DyeColor groundColor;
		if(favorite) {
			groundColor = DyeColor.LIME;
		}else {
		groundColor = DyeColor.WHITE;
		}
		switch(tier) {
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
