package net.wargearworld.bau.tools.testBlockSlave.testBlock;

import org.bukkit.block.BlockFace;

public enum Facing {
	NORTH,SOUTH;
	
	public String getFace() {
		switch(this){
			case NORTH:
				return "N";
			case SOUTH:
				return "S";
		}
		return null;
	}
	
	public static Facing getByShort(String shortName) {
		switch(shortName.toLowerCase()) {
		case "n": return NORTH;
		case "s": return SOUTH;
		}
		return null;
	}

	
	public  BlockFace toBlockFace() {
		switch(this){
		case NORTH:
			return BlockFace.NORTH;
		case SOUTH:
			return BlockFace.SOUTH;
	}
	return null;
	}
}
