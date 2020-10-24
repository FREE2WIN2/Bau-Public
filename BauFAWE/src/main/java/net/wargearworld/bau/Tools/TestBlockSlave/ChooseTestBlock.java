package net.wargearworld.bau.tools.testBlockSlave;

import net.wargearworld.bau.tools.testBlockSlave.testBlock.DefaultTestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.ITestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.TestBlockType;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Type;

public class ChooseTestBlock {
	private ITestBlock block;
	private Facing facing;
	private Type type;
	private int tier;
	private TestBlockType testBlockType;
	public ChooseTestBlock() {
		block = null;
		facing = null;
		type = null;
		testBlockType = null;
	}
	
	/* SETTER */
	
	public ChooseTestBlock setTier(int tier) {
		this.tier = tier;
		return this;
	}
	public ChooseTestBlock setType(Type type) {
		this.type = type;
		return this;
	}
	public ChooseTestBlock setTestBlockType(TestBlockType type) {
		this.testBlockType = type;
		return this;
	}
	public ChooseTestBlock setFacing(Facing facing) {
		this.facing = facing;
		return this;
	}	
	public ChooseTestBlock setTestBlock(ITestBlock block) {
		this.block = block;
		return this;
	}
	
	/* GETTER */
	
	public Facing getFacing() {
		return facing;
	}	
	
	public int getTier() {
		return tier;
	}
	
	public Type getType() {
		return type;
	}
	
	public TestBlockType getTestBlockType() {
		return testBlockType;
	}
	
	
	public ITestBlock getTestBlock() {
		if(block !=null) {
			return block;
		}
		if(testBlockType == TestBlockType.DEFAULT) {
			return DefaultTestBlock.getByChooseTestBlock(this);
		}
		return null;
	}
}
