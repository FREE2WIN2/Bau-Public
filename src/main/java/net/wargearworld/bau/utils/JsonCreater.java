package net.wargearworld.bau.utils;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;

public class JsonCreater {
	String jsontext;
	int addedJsons;

	public JsonCreater(String text) {
		jsontext = "{\"text\":\"" + text + "\"";
		addedJsons = 0;
	}

	public JsonCreater addClickEvent(String command, ClickAction action) {
		String actions = action.getAction();
		String clickJson = ",\"clickEvent\":{\"action\":\"" + actions + "\",\"value\":\"" + command + "\"}";
		jsontext += clickJson;
		return this;
	}

	public JsonCreater addHoverEvent(String text) {
		String hoverJson = ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + text + "\"}}";
		jsontext += hoverJson;
		return this;
	}

	private String create() {
		for (int i = 0; i < addedJsons; i++) {
			jsontext += "}]";
		}
		jsontext += "}";
		return jsontext;
	}

	public JsonCreater addJson(JsonCreater json) {
		jsontext += ",\"extra\":[" + json.getText();
		addedJsons++;
		addedJsons += json.getAddedJsons();
		return this;
	}

	public String getText() {
		return jsontext;
	}

	public int getAddedJsons() {
		return addedJsons;
	}

	public void send(Player p) {
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(create())));
	}

}
