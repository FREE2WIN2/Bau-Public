package de.AS.Bau.Tools.Particles;

import org.bukkit.Color;

public enum Colors {

	AQUA, BLACK, BLUE, FUCHSIA, GRAY, GREEN, LIME, MAROON, NAVY, OLIVE, ORANGE, PURPLE, RED, SILVER, TEAL, WHITE, YELLOW;
	
	public Color getColor() {
		switch(this) {
		case AQUA:
			return Color.AQUA;
		case BLACK:
			return Color.BLACK;
		case BLUE:
			return Color.BLUE;
		case FUCHSIA:
			return Color.FUCHSIA;
		case GRAY:
			return Color.GRAY;
		case GREEN:
			return Color.GREEN;
		case LIME:
			return Color.LIME;
		case MAROON:
			return Color.MAROON;
		case NAVY:
			return Color.NAVY;
		case OLIVE:
			return Color.OLIVE;
		case ORANGE:
			return Color.ORANGE;
		case PURPLE:
			return Color.PURPLE;
		case RED:
			return Color.RED;
		case SILVER:
			return Color.SILVER;
		case TEAL:
			return Color.TEAL;
		case WHITE:
			return Color.WHITE;
		case YELLOW:
			return Color.YELLOW;
		}
		return null;
	}
	
	public static Colors getByColor(Color color) {
		if(color.equals(Color.AQUA)) {
			return AQUA;
		}else if(color.equals(Color.BLACK)) {
			return BLACK;
		}else if(color.equals(Color.BLUE)) {
			return BLUE;
		}else if(color.equals(Color.FUCHSIA)) {
			return FUCHSIA;
		}else if(color.equals(Color.GRAY)) {
			return GRAY;
		}else if(color.equals(Color.GREEN)) {
			return GREEN;
		}else if(color.equals(Color.LIME)) {
			return LIME;
		}else if(color.equals(Color.MAROON)) {
			return MAROON;
		}else if(color.equals(Color.NAVY)) {
			return NAVY;
		}else if(color.equals(Color.OLIVE)) {
			return OLIVE;
		}else if(color.equals(Color.ORANGE)) {
			return ORANGE;
		}else if(color.equals(Color.PURPLE)) {
			return PURPLE;
		}else if(color.equals(Color.RED)) {
			return RED;
		}else if(color.equals(Color.SILVER)) {
			return SILVER;
		}else if(color.equals(Color.TEAL)) {
			return TEAL;
		}else if(color.equals(Color.WHITE)) {
			return WHITE;
		}else if(color.equals(Color.YELLOW)) {
			return YELLOW;
		}
		return null;
	}
}
