package de.AS.Bau.Tools.TestBlockSlave.TestBlock;

public enum Type {
	NORMAL,SHIELDS,FRAME;
	
	public static Type fromString(String s) {
		switch(s.toLowerCase()) {
		case "n":
			return NORMAL;
		case "s":
			return SHIELDS;
		case "f":
			return FRAME;
		}
		return null;
	}
	
	public String getShort() {
		switch(this) {
		case FRAME:
			return "F";
		case NORMAL:
			return "N";
		case SHIELDS:
			return "S";
		}
		return null;
	}
}
