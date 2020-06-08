package net.wargearworld.Bau;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.utils.Language;

public class StringGetterBau {
	public static HashMap<UUID, Language> playersLanguage = new HashMap<>();
	private static Properties english;
	private static Properties german;
	
	public StringGetterBau(){
		try {
			english = new Properties();
			InputStream in = StringGetterBau.class.getResourceAsStream("/language_en.properties");
			english.load(in);
			in.close();
			german = new Properties();
			in = StringGetterBau.class.getResourceAsStream("/language_de.properties");
			german.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getString(Player p, String name) {
		return getString(p.getUniqueId(), name);
	}

	public static String getString(String uuid, String name) {
		return getString(UUID.fromString(uuid),name);
	}
	
	public static String getString(UUID uuid, String name) {
		if (playersLanguage.containsKey(uuid)) {
			if (playersLanguage.get(uuid).equals(Language.EN)) {
				return english.getProperty(name);
			}
		}else {
			if(Language.getLanguageByString(DBConnection.getLanguage(uuid.toString()))==Language.EN) {
				return english.getProperty(name);
			}
		}
		//standard
		return german.getProperty(name);
	}
	public static String getString(Player p, String name, String... args) {
		String msg = getString(p, name);
		for(String a:args) {
			msg = msg.replaceFirst("%r", a);
		}
		return msg;
	}
}
