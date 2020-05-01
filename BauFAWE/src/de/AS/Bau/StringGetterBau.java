package de.AS.Bau;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.bukkit.entity.Player;

public class StringGetterBau {
	public static HashMap<Player, String> playersLanguage = new HashMap<Player, String>();

	public static String getString(Player p, String name) {
		InputStream in;
		if (playersLanguage.containsKey(p)) {
			if (playersLanguage.get(p).equalsIgnoreCase("en")) {
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

	public static String getString(String uuid, String name) {
		DBConnection conn = new DBConnection();
		String lang = conn.getLanguage(uuid);
		InputStream in;
		if (lang.equalsIgnoreCase("en")) {
			in = StringGetterBau.class.getResourceAsStream("/language_en.properties");
		} else {
			in = StringGetterBau.class.getResourceAsStream("/language_de.properties");
		}

		Properties languagePropertie = new Properties();
		try {
			languagePropertie.load(in);
			String message = languagePropertie.getProperty(name);
			in.close();
			conn.closeConn();
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}
		conn.closeConn();
		return null;
	}
}
