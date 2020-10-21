package net.wargearworld.bau.tools.testBlockSlave.testBlock;


import net.wargearworld.bau.worldedit.Schematic;

public interface ITestBlock {

    public String getName();
	public Schematic getSchematic();
	public int getTier();
}
