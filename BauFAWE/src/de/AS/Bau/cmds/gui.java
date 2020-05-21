package de.AS.Bau.cmds;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.Tools.DesignTool;
import de.AS.Bau.Tools.Stoplag;

public class gui implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		// inventar öffnen mit
		// ->tnt
		// ->Testblock
		// ->tntTrails
		// ->plotClearer
		// -> gs verwaltung(Member sehen, löschen, hinzufügen,....s)
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
		if(p.getWorld().getName().contains("test")) {
			worldname = p.getWorld().getName();
		}else {
			worldname = conn.getName(p.getWorld().getName());
		}
		
		String inventoryName = StringGetterBau.getString(p,"inventoryName").replace("%r", worldname);
		Inventory inv = Bukkit.createInventory(null, 45, inventoryName);
//erstes item: tnt
		ItemStack tntIS = new ItemStack(Material.TNT);
		ItemMeta tntIM = tntIS.getItemMeta();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		String rgID = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
				.get(0);
		ProtectedRegion rg = regions.getRegion(rgID);

		if (rg.getFlag(Main.TntExplosion) == State.DENY) {
			tntIM.setDisplayName(StringGetterBau.getString(p,"tntDenied"));

		} else {
			tntIM.setDisplayName(StringGetterBau.getString(p,"tntAllowed"));
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
			sm.setDisplayName(StringGetterBau.getString(p,"guiMember"));
			playerHeadItem.setItemMeta(sm);
			inv.setItem(22, playerHeadItem);
			
			//delete
			ItemStack Barrier1IS = new ItemStack(Material.BARRIER);
			Barrier1IS.setAmount(1);
			ItemMeta Barrier1IM = Barrier1IS.getItemMeta();
			Barrier1IM.setDisplayName(StringGetterBau.getString(p,"deletePlot"));
			Barrier1IS.setItemMeta(Barrier1IM);
			inv.setItem(4, Barrier1IS);
		}
		// TestBlockSklave->Wolle
	
		// enderpearls
		ItemStack enderPearl1IS = new ItemStack(Material.ENDER_PEARL, 1);
		ItemMeta eIM1 = enderPearl1IS.getItemMeta();
		eIM1.setDisplayName(StringGetterBau.getString(p,"teleportPlotOne"));
		enderPearl1IS.setItemMeta(eIM1);
		inv.setItem(38, enderPearl1IS);
		// 2pearls
		ItemStack enderPearl2IS = new ItemStack(Material.ENDER_PEARL, 2);
		ItemMeta eIM2 = enderPearl2IS.getItemMeta();
		eIM2.setDisplayName(StringGetterBau.getString(p,"teleportPlotTwo"));
		enderPearl2IS.setItemMeta(eIM2);
		inv.setItem(40, enderPearl2IS);
		// 3pearls
		ItemStack enderPearl3IS = new ItemStack(Material.ENDER_PEARL, 3);
		ItemMeta eIM3 = enderPearl3IS.getItemMeta();
		eIM3.setDisplayName(StringGetterBau.getString(p,"teleportPlotThree"));
		enderPearl3IS.setItemMeta(eIM3);
		inv.setItem(42, enderPearl3IS);

		// torch für sl
		if (Stoplag.getStatus(p.getLocation())) {
			ItemStack torchAn = new ItemStack(Material.REDSTONE);
			ItemMeta torchAnMeta = torchAn.getItemMeta();
			torchAnMeta.setDisplayName(StringGetterBau.getString(p,"torchOff"));
			torchAn.setItemMeta(torchAnMeta);
			inv.setItem(24, torchAn);
		} else {
			ItemStack torchAus = new ItemStack(Material.GUNPOWDER);
			ItemMeta torchAusMeta = torchAus.getItemMeta();
			torchAusMeta.setDisplayName(StringGetterBau.getString(p,"torchOn"));
			torchAus.setItemMeta(torchAusMeta);
			inv.setItem(24, torchAus);
		}

		// rakete für track
		ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
		ItemMeta rocketIM = rocket.getItemMeta();
		rocketIM.setDisplayName(StringGetterBau.getString(p,"track"));
		rocket.setItemMeta(rocketIM);
		inv.setItem(12, rocket);
		
		ItemStack observer = new ItemStack(Material.OBSERVER);
		ItemMeta observerIM = observer.getItemMeta();
		observerIM.setDisplayName(StringGetterBau.getString(p,"show"));
		observer.setItemMeta(observerIM);
		inv.setItem(14, observer);
		
		//NetherStar
		ItemStack guiItem = new ItemStack(Material.NETHER_STAR);
		ItemMeta guiMeta = guiItem.getItemMeta();
		guiMeta.setDisplayName("§6GUI");
		guiItem.setItemMeta(guiMeta);
		inv.setItem(0, guiItem);
		inv.setItem(8, guiItem);
		inv.setItem(36, guiItem);
		inv.setItem(44, guiItem);
		//DS
		ItemStack ds = new ItemStack(Material.DEBUG_STICK);
		inv.setItem(28, ds);
		//TBS
		ItemStack TestMaterialIS = new ItemStack(Material.WHITE_WOOL);
		ItemMeta TestMaterialIM = TestMaterialIS.getItemMeta();
		TestMaterialIM.setDisplayName(StringGetterBau.getString(p,"testBlockSklaveGui"));
		TestMaterialIS.setItemMeta(TestMaterialIM);
		inv.setItem(30, TestMaterialIS);
		//DT
		String dtName;
		ItemStack dst = new ItemStack(Material.WOODEN_SHOVEL);
		ItemMeta dtIM = observer.getItemMeta();
		if(DesignTool.playerHasDtOn.get(p.getUniqueId())) {
			dtName = StringGetterBau.getString(p, "dtItemOn");
			dtIM.addEnchant(Enchantment.SILK_TOUCH, 1, true);
			dtIM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}else {
			dtName = StringGetterBau.getString(p, "dtItemOff");
		}
		dtIM.setDisplayName(dtName);
		dst.setItemMeta(dtIM);
		inv.setItem(32, dst);
		//FZ
		ItemStack fz = new ItemStack(Material.WOODEN_HOE);
		ItemMeta fzIM = observer.getItemMeta();
		fzIM.setDisplayName(StringGetterBau.getString(p,"fernzuender"));
		fz.setItemMeta(fzIM);
		inv.setItem(34, fz);
		// close DBConnection
		conn.closeConn();
		return inv;
	}
}
