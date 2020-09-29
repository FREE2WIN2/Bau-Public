package net.wargearworld.Bau.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.World.Plots.Plot;

public class BauPlayer {
	private static HashMap<UUID, BauPlayer> players = new HashMap<>();

	public static BauPlayer getBauPlayer(Player p) {
		if (p != null)
			return getBauPlayer(p.getUniqueId());
		return null;
	}

	public static BauPlayer getBauPlayer(UUID uuid) {
		BauPlayer player = players.get(uuid);
		if (player == null) {
			player = new BauPlayer(uuid);
			players.put(uuid, player);
		}
		return player;
	}

	public static void remove( UUID uniqueId) {
		players.remove(uniqueId);
	}
	
	private UUID uuid;
	FileConfiguration config;
	File configFile;

	private BauPlayer(UUID uuid) {
		this.uuid = uuid;
		configFile = new File(Main.getPlugin().getDataFolder(), "users/" + uuid.toString() + "/settings.yml");
		config = new YamlConfiguration();
		try {
		if(!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			configFile.createNewFile();
			Files.copy(BauPlayer.class.getResourceAsStream("playerDefaults.yml"), configFile.toPath());
		}
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public Player getBukkitPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public String getName() {
		Player p = getBukkitPlayer();
		if (p == null) {
			return DBConnection.getName(getUuid().toString());
		}
		return p.getName();
	}

	private void save() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* Stoplag */

	public void setSLPaste(boolean active) {
		config.set("stoplag.paste", active);
		save();
	}

	public void setSLPasteTime(Integer time) {
		config.set("stoplag.pastetime", time);
		save();
	}

	public boolean getPasteState() {
		return config.getBoolean("stoplag.paste");
	}

	public int getPasteTime() {
		return config.getInt("stoplag.pastetime");
	}

	public Plot getCurrentPlot() {
		Location loc = getBukkitPlayer().getLocation();
		return WorldManager.get(loc.getWorld()).getPlot(loc);
	}

	public boolean toggleDT() {
		setDT(!getDT());
		return getDT();

	}

	public void setDT(boolean on) {
		config.set("dt", on);
		save();
	}

	public boolean getDT() {
		return config.getBoolean("dt");
	}

	public void sendMemberedGS() {
		Player p = getBukkitPlayer();
		if(p== null)
			return;
		
		ArrayList<String> memberedPlots = new ArrayList<>();
		memberedPlots.add(p.getName());
		memberedPlots.addAll(DBConnection.getMemberedPlots(p.getUniqueId()));
		PlayerConnection pConn = ((CraftPlayer) p).getHandle().playerConnection;
		p.sendMessage(MessageHandler.getInstance().getString(p, "listGsHeading"));
		for (String string : memberedPlots) {
			/* s == PlayerName */
			String hover = MessageHandler.getInstance().getString(p, "listGsHover").replace("%r", string);
			String name = string;
			String txt = "{\"text\":\"§7[§6" + name
					+ "§7]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/gs tp " + name
					+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + hover + "\"}}}";
			IChatBaseComponent txtc = ChatSerializer.a(txt);
			PacketPlayOutChat txtp = new PacketPlayOutChat(txtc);
			pConn.sendPacket(txtp);
		}
		p.sendMessage("§7----------------------------");
	}

	public void sendMessage(String message) {
		Player p = getBukkitPlayer();
		if(p== null)
			return;
		p.sendMessage(message);
	}

	

}
