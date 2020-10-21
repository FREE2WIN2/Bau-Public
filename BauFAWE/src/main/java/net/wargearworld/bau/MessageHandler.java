package net.wargearworld.bau;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.bau.player.BauPlayer;
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
	private HashMap<Language, Properties> props;

	public MessageHandler() {
		instance = this;
		props = new HashMap<>();
		for (Language lang : Language.values()) {
			try {
				Properties prop = new Properties();
				InputStreamReader in = new InputStreamReader(MessageHandler.class.getResourceAsStream(
						"/langPacks/language_" + lang.name().toLowerCase() + ".properties"), "UTF-8");
				prop.load(in);
				props.put(lang, prop);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getString(Player p, String name) {
		return getString(p.getUniqueId(), name);
	}

	@Override
	public String getString(UUID uuid, String name, String... args) {
		Language lang = playersLanguage.get(uuid);
		if (lang == null)
			lang = DBConnection.getLanguage(uuid);
		String message = props.get(lang).getProperty(name);
		// standard
		for (String a : args) {
			message = message.replaceFirst("%r", a);
		}
		return message;
	}

	public Language getLanguage(Player p) {
		return playersLanguage.get(p.getUniqueId());
	}

	@Override
	public String getStringWithPrefix(Player p, String name, String... args) {
		return Main.prefix + getString(p, name, args);
	}

	@Override
	public String getString(String language, String name, String... args) {
		return getString(Language.valueOf(language.toUpperCase()), name, args);
	}

	@Override
	public String getString(Language lang, String name, String... args) {
		String message = props.get(lang).getProperty(name);
		for (String a : args) {
			message = message.replaceFirst("%r", a);
		}
		return message;
	}

	@Override
	public Language getLanguage(UUID uuid) {
		return playersLanguage.get(uuid);
	}

	public String getString(BauPlayer p, String name, String... args) {
		return getString(p.getUuid(), name, args);
	}

	@Override
	public String getString(Player p, String name, String... args) {
		return getString(p.getUniqueId(), name, args);
	}
	/* Sending Messages */
	@SuppressWarnings("deprecation")
	public void sendHotBar(Player p, String key, String... args) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(getString(p, key, args)));
	}
	public void send(Player p, String key, String... args) {
		p.sendMessage(getStringWithPrefix(p, key, args));
	}
	public void send(BauPlayer p, String key, String... args) {
		Player player = p.getBukkitPlayer();
		if(player == null)
			return;
		player.sendMessage(Main.prefix + getString(player, key, args));
	}

    public void send(UUID owner, String key, String... args) {
		send(BauPlayer.getBauPlayer(owner),key,args);
    }
}
