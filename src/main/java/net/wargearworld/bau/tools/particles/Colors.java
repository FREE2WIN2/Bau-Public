package net.wargearworld.bau.tools.particles;

import net.wargearworld.command_manager.ArgumentType;
import net.wargearworld.command_manager.arguments.EnumArgument;
import net.wargearworld.command_manager.arguments.EnumArgumentInterface;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.w3c.dom.Entity;

import java.util.ArrayList;
import java.util.List;

public enum Colors {

	AQUA, BLACK, BLUE, BROWN, CYAN, FUCHSIA, GRAY, GREEN, LIGHT_BLUE, LIGHT_GRAY, LIME,MAGENTA, MAROON, NAVY, OLIVE, ORANGE,PINK, PURPLE, RED, SILVER, TEAL, WHITE,
	YELLOW;



	public Color getColor() {
		switch (this) {
			case AQUA:
				return Color.AQUA;
			case BLACK:
				return Color.BLACK;
			case BLUE:
				return Color.BLUE;
			case BROWN:
				return DyeColor.BROWN.getColor();
			case CYAN:
				return DyeColor.CYAN.getColor();
			case FUCHSIA:
				return Color.FUCHSIA;
			case GRAY:
				return Color.GRAY;
			case GREEN:
				return Color.GREEN;
			case LIME:
				return Color.LIME;
			case LIGHT_BLUE:
				return DyeColor.LIGHT_BLUE.getColor();
			case LIGHT_GRAY:
				return DyeColor.LIGHT_GRAY.getColor();
			case MAGENTA:
				return DyeColor.MAGENTA.getColor();
			case MAROON:
				return Color.MAROON;
			case NAVY:
				return Color.NAVY;
			case OLIVE:
				return Color.OLIVE;
			case ORANGE:
				return Color.ORANGE;
			case PINK:
				DyeColor.PINK.getColor();
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

	public static String[] getPossibleOptions() {
		List<String> outlist = new ArrayList<>();
		for(Colors color:Colors.values()){
			outlist.add(color.name());
		}
		return outlist.toArray(new String[outlist.size()]);
	}

	static public EnumArgument<Colors> asArgument(){
		return new EnumArgument<Colors>(new EnumArgumentInterface<Colors>() {

			@Override
			public Colors fromString(String s) {
				return Colors.valueOf(s.toUpperCase());
			}

			@Override
			public String getTypeName() {
				return "Color";///TODO: make bilingual
			}

			@Override
			public String[] getPossibleOptions() {
				return Colors.getPossibleOptions();
			}

		});

	}
}