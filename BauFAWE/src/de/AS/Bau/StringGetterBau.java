package de.AS.Bau;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.bukkit.entity.Player;

public class StringGetterBau {
	public static HashMap<UUID, String> playersLanguage = new HashMap<>();

	public static String getString(Player p, String name) {
		return getString(p.getUniqueId(), name);
	}

	public static String getString(String uuid, String name) {
		return getString(UUID.fromString(uuid),name);
	}
	
	public static String getString(UUID uuid, String name) {
		InputStream in;
		if (playersLanguage.containsKey(uuid)) {
			if (playersLanguage.get(uuid).equalsIgnoreCase("en")) {
				in = StringGetterBau.class.getResourceAsStream("/language_en.properties");
			} else {
				in = StringGetterBau.class.getResourceAsStream("/language_de.properties");
			}
		} else {
			in = StringGetterBau.class.getResourceAsStream("/language_de.properties");
		}

		Properties languagePropertie = new Properties();
		try {
			languagePropertie.load(in);
			String message = languagePropertie.getProperty(name);
			in.close();
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
