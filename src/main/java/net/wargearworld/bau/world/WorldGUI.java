package net.wargearworld.bau.world;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.ItemBuilder;
import net.wargearworld.GUI_API.Items.ItemType;
import net.wargearworld.bau.MessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
public class WorldGUI implements Listener {

	public WorldGUI(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public static void openMain(Player p) {
		GUI mainGUI = new ChestGUI(27, MessageHandler.getInstance().getString(p, "worldGUI_title"));
		ItemBuilder.build(s->{s.getPlayer().closeInventory();}, Material.WOODEN_HOE	,1, ItemType.DEFAULT, "ItemName");
		HeadItem item = new HeadItem(new CustomHead("world"),s->{
			s.getPlayer().sendMessage(s.getClicked().getItemMeta().getDisplayName());
		});
		item.setCustomSkull(new CustomHead(1234));
		item.setCancelled(false);
		mainGUI.setItem(1,s->{return s.hasPermission("permission");}, item);
		mainGUI.open(p);
	}


}
