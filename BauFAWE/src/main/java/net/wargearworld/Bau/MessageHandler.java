package net.wargearworld.Bau;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.StringGetter.IStringGetter;
import net.wargearworld.StringGetter.Language;

public class MessageHandler implements IStringGetter {
	private static MessageHandler instance;

	public static MessageHandler getInstance() {
		if (instance == null)
			new MessageHandler();
		return instance;
	}

	public static HashMap<UUID, Language> playersLanguage = new HashMap<>();
	private static Properties english;
	private static Properties german;

	public MessageHandler() {
		instance = this;
		try {
			english = new Properties();
			InputStream in = MessageHandler.class.getResourceAsStream("/language_en.properties");
			english.load(in);
			in.close();
			german = new Properties();
			in = MessageHandler.class.getResourceAsStream("/language_de.properties");
			german.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getString(Player p, String name) {
		return getString(p.getUniqueId(), name);
	}

	public String getString(String uuid, String name) {
		return getString(UUID.fromString(uuid), name);
	}

	public String getString(UUID uuid, String name) {
		if (playersLanguage.containsKey(uuid)) {
			if (playersLanguage.get(uuid).equals(Language.EN)) {
				return english.getProperty(name);
			}
		} else {
			if (Language.valueOf(DBConnection.getLanguage(uuid.toString()).toUpperCase()) == Language.EN) {
				return english.getProperty(name);
			}
		}
		// standard
		return german.getProperty(name);
	}

	public String getString(Player p, String name, String... args) {
		String msg = getString(p, name);
		for (String a : args) {
			msg = msg.replaceFirst("%r", a);
		}
		return msg;
	}

	@SuppressWarnings("deprecation")
	public void sendHotBar(Player p, String key, String... args) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(getString(p, key, args)));
	}

	public Language getLanguage(Player p) {
		return playersLanguage.get(p.getUniqueId());
	}

	@Override
	public String getStringWithPrefix(Player p, String name, String... args) {
		return Main.prefix + getString(p, name, args);
	}

	@Override
	public String getString(UUID uuid, String name, String... args) {
		return Main.prefix + getString(uuid, name, args);
	}

	@Override
	public String getString(String language, String name, String... args) {
		return getString(Language.valueOf(language.toUpperCase()), name, args);
	}

	@Override
	public String getString(Language lang, String name, String... args) {
		String message;
		if (lang == Language.EN) {
			message = english.getProperty(name);
		} else {
			message = german.getProperty(name);
		}
		for (String a : args) {
			message = message.replaceFirst("%r", a);
		}
		return message;
	}

	@Override
	public Language getLanguage(UUID uuid) {
		return playersLanguage.get(uuid);
	}

	public String getString(BauPlayer p, String name,String...args) {
		return getString(p.getUuid(), name,args);
	}
}
