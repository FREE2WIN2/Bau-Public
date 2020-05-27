package de.AS.Bau.utils;

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
}
