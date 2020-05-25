package de.AS.Bau.Tools.TestBlockSlave;

import java.io.File;
import java.util.UUID;

import de.AS.Bau.Main;

public class TestBlock {

	/*
	 * This is a TestBlock out of the personal .yml of the Player.
	 * 
	 */ 
	
	private UUID owner;
	private File schematic;
	private String Name;
	private int tier;

	public TestBlock(UUID owner, String schemName, String Name, int tier) {
		this.owner = owner;
		this.schematic = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + owner.toString()
				+ "/TestBlockSklave/" + schemName);
		this.Name = Name;
		this.tier = tier;
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

	public File getSchem() {
		return schematic;
	}

}
