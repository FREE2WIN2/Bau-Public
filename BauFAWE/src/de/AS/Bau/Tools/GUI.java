package de.AS.Bau.Tools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.WorldEdit.WorldGuardHandler;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.ItemStackCreator;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;

public class GUI implements CommandExecutor, Listener {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		Inventory inv;
		if (sender instanceof Player) {
			Player p = (Player) sender;

			inv = inventarErstellen(p);
			p.openInventory(inv);
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public Inventory inventarErstellen(Player p) {
		DBConnection conn = new DBConnection();
		String worldname;
		if (p.getWorld().getName().contains("test")) {
			worldname = p.getWorld().getName();
		} else {
			worldname = conn.getName(p.getWorld().getName());
		}

		String inventoryName = StringGetterBau.getString(p, "inventoryName").replace("%r", worldname);
		Inventory inv = Bukkit.createInventory(null, 45, inventoryName);
//erstes item: tnt
		ItemStack tntIS = new ItemStack(Material.TNT);
		ItemMeta tntIM = tntIS.getItemMeta();
		ProtectedRegion rg = WorldGuardHandler.getRegion(p.getLocation());
		if (rg.getFlag(Main.TntExplosion) == State.DENY) {
			tntIM.setDisplayName(StringGetterBau.getString(p, "tntDenied"));

		} else {
			tntIM.setDisplayName(StringGetterBau.getString(p, "tntAllowed"));
			tntIM.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, true);
			tntIM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		tntIS.setItemMeta(tntIM);
		inv.setItem(20, tntIS);
//barriers
		// playerHead
		if (rg.getOwners().contains(p.getUniqueId()) || p.getUniqueId().toString().equals(p.getWorld().getName())) {
			ItemStack playerHeadItem = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta sm = (SkullMeta) playerHeadItem.getItemMeta();
			sm.setOwner(p.getName());
			sm.setDisplayName(StringGetterBau.getString(p, "guiMember"));
			playerHeadItem.setItemMeta(sm);
			inv.setItem(22, playerHeadItem);

			// delete
			ItemStack Barrier1IS = ItemStackCreator.createNewItemStack(Material.BARRIER,
					StringGetterBau.getString(p, "deletePlot"));
			inv.setItem(4, Barrier1IS);
		}
		// TestBlockSklave->Wolle

		// enderpearls
		ItemStack enderPearl1IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotOne"));
		inv.setItem(38, enderPearl1IS);
		// 2pearls
		ItemStack enderPearl2IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotTwo"));
		enderPearl2IS.setAmount(2);
		inv.setItem(40, enderPearl2IS);
		// 3pearls
		ItemStack enderPearl3IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				StringGetterBau.getString(p, "teleportPlotThree"));
		enderPearl3IS.setAmount(3);
		inv.setItem(42, enderPearl3IS);

		// torch für sl
		if (Stoplag.getStatus(p.getLocation())) {
			ItemStack torchAn = ItemStackCreator.createNewItemStack(Material.REDSTONE,
					StringGetterBau.getString(p, "torchOff"));
			inv.setItem(24, torchAn);
		} else {
			ItemStack torchAus = ItemStackCreator.createNewItemStack(Material.GUNPOWDER,
					StringGetterBau.getString(p, "torchOn"));
			inv.setItem(24, torchAus);
		}

		ItemStack rocket = ItemStackCreator.createNewItemStack(Material.FIREWORK_ROCKET,
				StringGetterBau.getString(p, "track"));
		inv.setItem(12, rocket);

		ItemStack observer = ItemStackCreator.createNewItemStack(Material.OBSERVER,
				StringGetterBau.getString(p, "show"));
		inv.setItem(14, observer);

		// NetherStar
		ItemStack guiItem = ItemStackCreator.createNewItemStack(Material.NETHER_STAR, "§6GUI");
		inv.setItem(0, guiItem);
		inv.setItem(8, guiItem);
		inv.setItem(36, guiItem);
		inv.setItem(44, guiItem);
		// DS
		ItemStack ds = new ItemStack(Material.DEBUG_STICK);
		inv.setItem(28, ds);
		// TBS
		ItemStack TestMaterialIS = ItemStackCreator.createNewItemStack(Material.WHITE_WOOL, StringGetterBau.getString(p, "testBlockSklaveGui"));
		inv.setItem(30, TestMaterialIS);
		// DT
		String dtName;
		ItemStack dst = new ItemStack(Material.WOODEN_SHOVEL);
		ItemMeta dtIM = observer.getItemMeta();
		if (DesignTool.playerHasDtOn.get(p.getUniqueId())) {
			dtName = StringGetterBau.getString(p, "dtItemOn");
			dtIM.addEnchant(Enchantment.SILK_TOUCH, 1, true);
			dtIM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		} else {
			dtName = StringGetterBau.getString(p, "dtItemOff");
		}
		dtIM.setDisplayName(dtName);
		dst.setItemMeta(dtIM);
		inv.setItem(32, dst);
		// FZ
		ItemStack fz = ItemStackCreator.createNewItemStack(FernzuenderListener.toolMaterial, StringGetterBau.getString(p, "fernzuender"));
		inv.setItem(34, fz);
		// close DBConnection
		conn.closeConn();
		return inv;
	}

	@EventHandler
	public void onInventoryclick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Inventory pInv = p.getOpenInventory().getTopInventory();
		String invName = p.getOpenInventory().getTitle();
		ItemStack clicked = event.getCurrentItem();
		if (clicked != null) {
			if (clicked.getType().equals(Material.NETHER_STAR) || clicked.getType().equals(FernzuenderListener.toolMaterial)||clicked.getType().equals(AutoTntReloader.toolMaterial)) {
				return;
			}
			if (clicked.hasItemMeta()) {
				if (!pInv.getType().equals(InventoryType.CREATIVE)) {
					String worldname;
					if(p.getWorld().getName().contains("test")) {
						worldname = p.getWorld().getName();
					}else {
						DBConnection conn = new DBConnection();
						worldname = conn.getName(p.getWorld().getName());
						conn.closeConn();
					}
					if (invName
							.equals(StringGetterBau.getString(p, "inventoryName").replace("%r",
									worldname))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						inventoryGUI(p, pInv, clicked);
					}

				}
			}
		}

	}

	private void inventoryGUI(Player p, Inventory pInv, ItemStack clicked) {
		String tntdenied = StringGetterBau.getString(p, "tntDenied");
		String tntallowed = StringGetterBau.getString(p, "tntAllowed");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String reset = StringGetterBau.getString(p, "deletePlot");
		String tp1 = StringGetterBau.getString(p, "teleportPlotOne");
		String tp2 = StringGetterBau.getString(p, "teleportPlotTwo");
		String tp3 = StringGetterBau.getString(p, "teleportPlotThree");
		String memberGUI = StringGetterBau.getString(p, "guiMember");
		String testBlockSklaveGui = StringGetterBau.getString(p, "testBlockSklaveGui");
		String trailShow = StringGetterBau.getString(p, "show");
		String dtItemOn = StringGetterBau.getString(p, "dtItemOff");
		String dtItemOff = StringGetterBau.getString(p, "dtItemOn");
		if (clickedName.equals(tntallowed)) {
			p.closeInventory();
			p.performCommand("tnt");
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "tntDeniedMessage"));
		} else if (clickedName.equals(tntdenied)) {
			p.closeInventory();
			p.performCommand("tnt");
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "tntAllowedMessage"));
		}else if(clickedName.equals(reset)){
			p.closeInventory();
			String rgID = WorldGuardHandler.getPlotId(p.getLocation());
			PlotResetter.resetRegion(rgID, p, false);
		} else if (clickedName.equals(tp1)) {
			p.closeInventory();
			p.teleport(CoordGetter.getTeleportLocation(p.getWorld(),"plot1"));
		} else if (clickedName.equals(tp2)) {
			p.closeInventory();
			p.teleport(CoordGetter.getTeleportLocation(p.getWorld(),"plot2"));
		} else if (clickedName.equals(tp3)) {
			p.closeInventory();
			p.teleport(CoordGetter.getTeleportLocation(p.getWorld(),"plot3"));
		} else if (clickedName.equals(memberGUI)) {
			p.closeInventory();
			showMember(p);
		} else if (clickedName.equals(testBlockSklaveGui)) {
			p.performCommand("tbs");
		} else if (clickedName.equals(StringGetterBau.getString(p, "torchOn"))
				|| clickedName.equals(StringGetterBau.getString(p, "torchOff"))) {
			// stoplag
			p.closeInventory();
			p.performCommand("sl");

		} else if (clickedName.equals(StringGetterBau.getString(p, "track"))) {
			p.performCommand("trail new");
			p.closeInventory();
		} else if (clickedName.equals(trailShow)) {
			p.performCommand("trail show");
			p.closeInventory();
		} else if (clickedName.equals(dtItemOff) || clickedName.equals(dtItemOn)) {
			p.performCommand("dt");
			p.closeInventory();
		}

	}

	public static void showMember(Player p) {
		PlayerConnection pConn = ((CraftPlayer) p).getHandle().playerConnection;
		DBConnection conn = new DBConnection();
		ResultSet rs = conn.getMember(p.getUniqueId().toString());
		ArrayList<String> memberlist = new ArrayList<>();
		try {

			while (rs.next()) {
				memberlist.add(conn.getName(rs.getString(1)));
			}
			p.sendMessage(StringGetterBau.getString(p, "memberListHeader").replace("%r", p.getName()));
			for (String memberName : memberlist) {
				String hover = StringGetterBau.getString(p, "memberHoverRemove").replace("%r", memberName);
				String add;
				add = "{\"text\":\"§7[§6" + memberName
						+ "§7] \",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/gs remove " + memberName
						+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + hover + "\"}}}";

				PacketPlayOutChat listp = new PacketPlayOutChat(ChatSerializer.a(add));
				pConn.sendPacket(listp);
			}
			String addMember = "{\"text\":\"§a[+]§r  \",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/gs add \"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
					+ StringGetterBau.getString(p, "addMemberHover") + "\"}}}";
			PacketPlayOutChat addMemberp = new PacketPlayOutChat(ChatSerializer.a(addMember));
			pConn.sendPacket(addMemberp);
			conn.closeConn();
		} catch (SQLException e) {
			e.printStackTrace();
			conn.closeConn();
		}
	}

}
