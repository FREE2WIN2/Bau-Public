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
}
