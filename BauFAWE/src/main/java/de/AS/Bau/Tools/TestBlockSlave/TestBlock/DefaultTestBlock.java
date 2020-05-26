package de.AS.Bau.Tools.TestBlockSlave.TestBlock;

import de.AS.Bau.WorldEdit.Schematic;
import de.AS.Bau.utils.Facing;

public class DefaultTestBlock implements TestBlock {

	
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

}
