package de.AS.Bau.Tools.Particles;

import org.bukkit.Color;

public enum Colors {

	AQUA, BLACK, BLUE, FUCHSIA, GRAY, GREEN, LIME, MAROON, NAVY, OLIVE, ORANGE, PURPLE, RED, SILVER, TEAL, WHITE,
	YELLOW;

	public Color getColor() {
		switch (this) {
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
		if (color.asRGB() == Color.AQUA.asRGB()) {
			return Colors.AQUA;
		} else if (color.asRGB() == Colors.BLACK.getColor().asRGB()) {
			return Colors.BLACK;
		} else if (color.asRGB() == Color.BLUE.asRGB()) {
			return Colors.BLUE;
		} else if (color.asRGB() == Color.FUCHSIA.asRGB()) {
			return Colors.FUCHSIA;
		} else if (color.asRGB() == Color.GRAY.asRGB()) {
			return Colors.GRAY;
		} else if (color.asRGB() == Color.GREEN.asRGB()) {
			return Colors.GREEN;
		} else if (color.asRGB() == Color.LIME.asRGB()) {
			return Colors.LIME;
		} else if (color.asRGB() == Color.MAROON.asRGB()) {
			return Colors.MAROON;
		} else if (color.asRGB() == Color.NAVY.asRGB()) {
			return Colors.NAVY;
		} else if (color.asRGB() == Color.OLIVE.asRGB()) {
			return Colors.OLIVE;
		} else if (color.asRGB() == Color.ORANGE.asRGB()) {
			return Colors.ORANGE;
		} else if (color.asRGB() == Color.PURPLE.asRGB()) {
			return Colors.PURPLE;
		} else if (color.asRGB() == Color.RED.asRGB()) {
			return Colors.RED;
		} else if (color.asRGB() == Color.SILVER.asRGB()) {
			return Colors.SILVER;
		} else if (color.asRGB() == Color.TEAL.asRGB()) {
			return Colors.TEAL;
		} else if (color.asRGB() == Color.WHITE.asRGB()) {
			return Colors.WHITE;
		} else if (color.asRGB() == Color.YELLOW.asRGB()) {
			return Colors.YELLOW;
		}
		return Colors.YELLOW;
	}
}
