package net.wargearworld.Bau.World;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.ItemBuilder;
import net.wargearworld.GUI_API.Items.ItemType;
import net.wargearworld.StringGetter.Language;


public class WorldGUI implements TabExecutor, Listener {

	private CommandHandel handle;

	public WorldGUI(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getCommand("worlds").setExecutor(this);
		plugin.getCommand("worlds").setTabCompleter(this);
		handle = new CommandHandel("worlds", Main.prefix);

		handle.setCallback(s -> {
			openMain(s.getPlayer());
		});
	}

	public static void openMain(Player p) {
		GUI mainGUI = new ChestGUI(27, MessageHandler.getInstance().getString(p, "worldGUI_title"));
		ItemBuilder.build(s->{s.getPlayer().closeInventory();}, Material.WOODEN_HOE	,1, ItemType.DEFAULT, "ItemName");
			HeadItem item = new HeadItem("world", 1);
		item.setExecutor(s -> {
			s.getPlayer().sendMessage(s.getClicked().getItemMeta().getDisplayName());
//			s.getPlayer().closeInventory();
		});
		item.setCustomSkull(CustomHead.GLOBE2);
		item.setCancelled(false);
		mainGUI.setItem(1,s->{return s.hasPermission("permission");}, item);
		mainGUI.open(p);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command arg1, String arg2, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			handle.execute(p, Language.DE, args);
		}
		return true;
	}
}
