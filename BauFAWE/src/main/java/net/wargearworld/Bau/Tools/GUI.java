package net.wargearworld.Bau.Tools;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.ItemStackCreator;
import net.wargearworld.Bau.utils.JsonCreater;

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
		String worldname = getName(p.getWorld());

		String inventoryName = MessageHandler.getInstance().getString(p, "inventoryName").replace("%r", worldname);
		Inventory inv = Bukkit.createInventory(null, 45, inventoryName);
//erstes item: tnt
		ItemStack tntIS = new ItemStack(Material.TNT);
		ItemMeta tntIM = tntIS.getItemMeta();
		ProtectedRegion rg = WorldGuardHandler.getRegion(p.getLocation());
		if (rg.getFlag(Main.TntExplosion) == State.DENY) {
			tntIM.setDisplayName(MessageHandler.getInstance().getString(p, "tntDenied"));

		} else {
			tntIM.setDisplayName(MessageHandler.getInstance().getString(p, "tntAllowed"));
			tntIM.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, true);
			tntIM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		tntIS.setItemMeta(tntIM);

//barriers
		// playerHead
		ItemStack playerHeadItem = new ItemStack(Material.AIR);
		ItemStack Barrier1IS = new ItemStack(Material.AIR);
		playerHeadItem = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta sm = (SkullMeta) playerHeadItem.getItemMeta();
		sm.setOwner(worldname);
		sm.setDisplayName(MessageHandler.getInstance().getString(p, "guiMember").replace("%r", worldname));
		playerHeadItem.setItemMeta(sm);

		// delete
		if (rg.getOwners().contains(p.getUniqueId()) || p.getUniqueId().toString().equals(p.getWorld().getName())||p.hasPermission("admin")) {
			Barrier1IS = ItemStackCreator.createNewItemStack(Material.BARRIER,
					MessageHandler.getInstance().getString(p, "deletePlot"));

		}
		// TestBlockSklave->Wolle

		// enderpearls
		ItemStack enderPearl1IS = ItemStackCreator.createNewItemStack(Material.ENDER_PEARL,
				MessageHandler.getInstance().getString(p, "gui_teleporter"));

		// torch f�r sl
		ItemStack stoplagIS;
		if (Stoplag.getStatus(p.getLocation())) {
			stoplagIS = ItemStackCreator.createNewItemStack(Material.REDSTONE,
					MessageHandler.getInstance().getString(p, "torchOff"));
		} else {
			stoplagIS = ItemStackCreator.createNewItemStack(Material.GUNPOWDER,
					MessageHandler.getInstance().getString(p, "torchOn"));
		}

		ItemStack observer = ItemStackCreator.createNewItemStack(Material.OBSERVER,
				MessageHandler.getInstance().getString(p, "tbs_gui_trail"));

		// NetherStar
		ItemStack guiItem = ItemStackCreator.createNewItemStack(Material.NETHER_STAR, "§6GUI");

		// DS
		ItemStack ds = new ItemStack(Material.DEBUG_STICK);

		// TBS
		ItemStack TestMaterialIS = ItemStackCreator.createNewItemStack(Material.WHITE_WOOL,
				MessageHandler.getInstance().getString(p, "testBlockSklaveGui"));

		// DT
		String dtName;
		ItemStack dst = new ItemStack(Material.WOODEN_SHOVEL);
		ItemMeta dtIM = observer.getItemMeta();
		if (DesignTool.playerHasDtOn.get(p.getUniqueId())) {
			dtName = MessageHandler.getInstance().getString(p, "dtItemOn");
			dtIM.addEnchant(Enchantment.SILK_TOUCH, 1, true);
			dtIM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		} else {
			dtName = MessageHandler.getInstance().getString(p, "dtItemOff");
		}
		dtIM.setDisplayName(dtName);
		dst.setItemMeta(dtIM);

		// FZ
		ItemStack fz = ItemStackCreator.createNewItemStack(Material
				.valueOf(Main.getPlugin().getCustomConfig().getString("fernzuender")),
				MessageHandler.getInstance().getString(p, "fernzuender"));

		ItemStack CannonReloader = ItemStackCreator.createNewItemStack(AutoCannonReloader.toolMaterial,
				MessageHandler.getInstance().getString(p, "cannonReloader_guiName"));

		// Particles

		/* setItems */
		inv.setItem(0, guiItem);
		inv.setItem(8, guiItem);
		inv.setItem(36, guiItem);
		inv.setItem(44, guiItem);

		inv.setItem(3, observer);
		inv.setItem(5, TestMaterialIS);
		inv.setItem(11, fz);
		inv.setItem(13, Barrier1IS);
		inv.setItem(15, CannonReloader);
		inv.setItem(19, tntIS);
		inv.setItem(22, playerHeadItem);
		inv.setItem(25, stoplagIS);
		inv.setItem(29, ds);
		inv.setItem(31, enderPearl1IS);

		inv.setItem(33, dst);
		
		inv.setItem(39, ItemStackCreator.createNewItemStack(Material.MELON_SEEDS,
				MessageHandler.getInstance().getString(p, "gui_particles")));
		inv.setItem(41, TntChest.getTNTChest());
		// close DBConnection
		return inv;
	}

	@EventHandler
	public void onInventoryclick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Inventory pInv = p.getOpenInventory().getTopInventory();
		String invName = p.getOpenInventory().getTitle();
		ItemStack clicked = event.getCurrentItem();
		if (clicked != null) {
			if (clicked.getType().equals(Material.NETHER_STAR)
					|| clicked.getType().equals(Material
							.valueOf(Main.getPlugin().getCustomConfig().getString("fernzuender")))
					|| clicked.getType().equals(AutoCannonReloader.toolMaterial)||clicked.getType() == Material.CHEST) {
				return;
			}
			if (clicked.hasItemMeta()) {
				if (!pInv.getType().equals(InventoryType.CREATIVE)) {
					String worldname;
					if (p.getWorld().getName().contains("test")) {
						worldname = p.getWorld().getName();
					} else {
						worldname = DBConnection.getName(p.getWorld().getName());
					}
					if (invName.equals(MessageHandler.getInstance().getString(p, "inventoryName").replace("%r", worldname))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						inventoryGUI(p, pInv, clicked);
					}

				}
			}
		}

	}

	private void inventoryGUI(Player p, Inventory pInv, ItemStack clicked) {
		String tntdenied = MessageHandler.getInstance().getString(p, "tntDenied");
		String tntallowed = MessageHandler.getInstance().getString(p, "tntAllowed");
		String clickedName = clicked.getItemMeta().getDisplayName();
		String reset = MessageHandler.getInstance().getString(p, "deletePlot");
		String tp1 = MessageHandler.getInstance().getString(p, "gui_teleporter");
		String testBlockSklaveGui = MessageHandler.getInstance().getString(p, "testBlockSklaveGui");
		String trailgui = MessageHandler.getInstance().getString(p, "tbs_gui_trail");
		String dtItemOn = MessageHandler.getInstance().getString(p, "dtItemOff");
		String dtItemOff = MessageHandler.getInstance().getString(p, "dtItemOn");
		if (clickedName.equals(tntallowed)) {
			p.closeInventory();
			p.performCommand("tnt");
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "tntDeniedMessage"));
		} else if (clickedName.equals(tntdenied)) {
			p.closeInventory();
			p.performCommand("tnt");
			p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "tntAllowedMessage"));
		} else if (clickedName.equals(reset)) {
			p.closeInventory();
			String rgID = WorldGuardHandler.getPlotId(p.getLocation());
			PlotResetter.resetRegion(rgID, p, false);
		} else if (clickedName.equals(tp1)) {
			PlotTeleporter.openInv(p);
		} else if (clickedName.equals(MessageHandler.getInstance().getString(p, "gui_particles"))) {
			p.performCommand("particles gui");
		} else if (clicked.getType().equals(Material.PLAYER_HEAD)) {
			p.closeInventory();
			p.performCommand("gs info");
		} else if (clickedName.equals(testBlockSklaveGui)) {
			p.performCommand("tbs");
		} else if (clickedName.equals(MessageHandler.getInstance().getString(p, "torchOn"))
				|| clickedName.equals(MessageHandler.getInstance().getString(p, "torchOff"))) {
			// stoplag
			p.closeInventory();
			p.performCommand("sl");
		} else if (clickedName.equals(trailgui)) {
			p.performCommand("trail gui");
		} else if (clickedName.equals(dtItemOff) || clickedName.equals(dtItemOn)) {
			p.performCommand("dt");
			p.closeInventory();
		}

	}

	public static void showMember(Player p) {
		boolean isOwner = p.getWorld().getName().equals(p.getUniqueId().toString());
		Set<String> memberlist = DBConnection.getMember(p.getUniqueId().toString());
		Main.send(p, "memberListHeader", getName(p.getWorld()));
		for (String memberUUID : memberlist) {
			String memberName = DBConnection.getName(memberUUID);
			String hover = MessageHandler.getInstance().getString(p, "memberHoverRemove").replace("%r", memberName);
			JsonCreater remove = new JsonCreater("§7[§6" + memberName + "§7]");
			if (isOwner) {
				remove.addClickEvent("/gs remove " + memberName, ClickAction.SUGGEST_COMMAND).addHoverEvent(hover);
			}
			remove.send(p);
		}
		if (isOwner) {
			new JsonCreater("§a[+]§r  ").addClickEvent("/gs add ", ClickAction.SUGGEST_COMMAND)
					.addHoverEvent(MessageHandler.getInstance().getString(p, "addMemberHover")).send(p);
		}

	}

	public static String getName(World w) {
		if (w.getName().contains("test")) {
			return w.getName();
		} else {
			return DBConnection.getName(w.getName());
		}
	}
}
