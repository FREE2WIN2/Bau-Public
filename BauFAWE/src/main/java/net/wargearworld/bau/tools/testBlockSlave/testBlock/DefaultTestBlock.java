package net.wargearworld.bau.tools.testBlockSlave.testBlock;

import java.util.HashMap;

import net.wargearworld.bau.tools.testBlockSlave.ChooseTestBlock;
import net.wargearworld.bau.worldedit.Schematic;

public class DefaultTestBlock implements TestBlock {

	public static HashMap<String, DefaultTestBlock> defaultTestBlocks = new HashMap<>();
	
	private String name;
	private Schematic schem;
	private int tier;
	public DefaultTestBlock(String name) {
		this.name = name;
		
		schem = new Schematic("TestBlockSklave",name + ".schem",Facing.NORTH); //DefaultFacing: North
		tier = Integer.parseInt(name.substring(1, 2));
	}

	@Override
	public String getName() {
		
		return name;
	}

	@Override
	public Schematic getSchematic() {
		
		return schem;
	}

	@Override
	public int getTier() {
		
		return tier;
	}

	public static void generateDefaultTestBlocks() {
		String[] tiers = new String[3];
		String[] types = new String[3];
		tiers[0] = "T1";
		tiers[1] = "T2";
		tiers[2] = "T3";

		types[0] = "S";
		types[1] = "N";
		types[2] = "F";
		for (String tier : tiers) {
			for (String type : types) {
				String name = tier + "_N_" + type;
				defaultTestBlocks.put(name, new DefaultTestBlock(name));
			}

		}
		System.out.println("Default TB loaded");
	}
	
	public static DefaultTestBlock getByChooseTestBlock(ChooseTestBlock block) {
		String key = "";
		key += "T" + block.getTier() + "_";
		key += "N_";
		key += block.getType().getShort();
		return defaultTestBlocks.get(key);
	}
	
}
