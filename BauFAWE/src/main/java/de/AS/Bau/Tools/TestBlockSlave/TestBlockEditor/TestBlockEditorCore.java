package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TestBlockEditorCore implements Listener{

	static Map<UUID,TestBlockEditor> playersTestBlockEditor;
	
	public TestBlockEditorCore() {
		playersTestBlockEditor = new HashMap<>();
	}
	
	@EventHandler
	public void invClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
	}
	
	public static TestBlockEditor getEditor(Player p) {
		if(!playersTestBlockEditor.containsKey(p.getUniqueId())) {
			playersTestBlockEditor.put(p.getUniqueId(), new TestBlockEditor(p));
		}
		return playersTestBlockEditor.get(p.getUniqueId());
	}
	
}
