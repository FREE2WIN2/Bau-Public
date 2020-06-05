package de.AS.Bau.utils;

public enum Language {
	EN, DE;
	public static Language getLanguageByString(String string) {
		if (string.equalsIgnoreCase("en")) {
			return EN;
		} else {
			return DE;
		}
	}
}