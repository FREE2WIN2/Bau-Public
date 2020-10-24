package net.wargearworld.bau.tools.testBlockSlave.testBlock;

import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.utils.Banner;

import java.util.UUID;

public class CustomTestBlock implements ITestBlock {

	/*
	 * This is a TestBlock out of the personal .yml of the Player.
	 * 
	 */

	private UUID owner;
	private Schematic schematic;
	private String Name;
	private int tier;
	private boolean favorite;
	private long id;

	public CustomTestBlock(UUID owner, String name, Facing facing, int tier) {
		this.owner = owner;
		this.Name = name;
		this.tier = tier;
		this.schematic = new Schematic(this.owner + "/TestBlockSklave", name + ".schem", facing);
		this.favorite = false;
	}

	public static CustomTestBlock fromDb(net.wargearworld.db.model.TestBlock block) {
		CustomTestBlock tb = new CustomTestBlock(block.getOwner().getUuid(),block.getName(), block.getName(),Facing.valueOf(block.getDirection().name()), block.getTier(), block.getFavorite(),block.getId());
		return tb;
	}


	/**
	 * Creates a new TestBlock
	 * 
	 * @param owner     -> UUID of the owner. If the TestBlock is a default one
	 *                  owner is not an empty String!
	 * @param schemName -> name of the schemfile
	 * @param Name      -> Name of the TestBlock
	 * @param tier      -> int-value of the tier
	 */
	public CustomTestBlock(UUID owner, String schemName, String Name, Facing face, int tier, boolean favorite, long id) {
		this.owner = owner;
		this.schematic = new Schematic(owner + "/TestBlockSklave", schemName, face);
		this.Name = Name;
		this.tier = tier;
		this.favorite = favorite;
		this.id = id;
	}

    public int getTier() {
		return tier;
	}

	public UUID getOwner() {
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

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
