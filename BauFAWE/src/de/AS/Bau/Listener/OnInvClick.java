package de.AS.Bau.Listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.utils.Banner;
import de.AS.Bau.utils.ClickAction;
import de.AS.Bau.utils.JsonCreater;
import de.AS.Bau.utils.Scheduler;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;

public class OnInvClick implements Listener {
	public static HashMap<Player, String> playerLastPaste = new HashMap<Player, String>();
	public static List<Player> playerBlockedDelete = new ArrayList<>();
	String aushwalString = "";

	public OnInvClick(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onInventoryclick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		Inventory pInv = p.getOpenInventory().getTopInventory();
		String invName = p.getOpenInventory().getTitle();
		ItemStack clicked = event.getCurrentItem();
		if (clicked != null) {
			if (clicked.getType().equals(Material.NETHER_STAR) || clicked.getType().equals(Material.WOODEN_HOE)) {
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
					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveTierInv"))
							&& event.getClickedInventory().equals(pInv)) {

						event.setCancelled(true);
						tierINventory(p, pInv, clicked);
					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveFacingInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						facingInv(p, pInv, clicked);

					} else if (invName.equals(StringGetterBau.getString(p, "testBlockSklaveTypeInv"))
							&& event.getClickedInventory().equals(pInv)) {
						event.setCancelled(true);
						typeInv(p, pInv, clicked);

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
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
			String rgID = regions.getApplicableRegionsIDs(
					BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())).get(0);
			resetRegion(rgID, p, false);
		} else if (clickedName.equals(tp1)) {
			p.closeInventory();
			p.teleport(new Location(p.getWorld(), -100, 8, 17));
		} else if (clickedName.equals(tp2)) {
			p.closeInventory();
			p.teleport(new Location(p.getWorld(), -208, 8, 17));
		} else if (clickedName.equals(tp3)) {
			p.closeInventory();
			p.teleport(new Location(p.getWorld(), -316, 8, 17));
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

	private void tierINventory(Player p, Inventory pInv, ItemStack clicked) {
		String close = StringGetterBau.getString(p, "close");
		String lastPaste = StringGetterBau.getString(p, "lastPaste");
		String clickedName = clicked.getItemMeta().getDisplayName();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		String rgID = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())).get(0);
		switch (clickedName) {
		case "§rTier I":
			aushwalString = "T1_";
			p.openInventory(richtungsInventory(p));
			break;
		case "§rTier II":
			aushwalString = "T2_";
			p.openInventory(richtungsInventory(p));
			break;
		case "§rTier III/IV":
		case "§rTier IV":
			aushwalString = "T3_";
			p.openInventory(richtungsInventory(p));
			break;
		}
		if (clickedName.equals(close)) {
			p.closeInventory();
		} else if (clickedName.equals(lastPaste)) {
			if (playerLastPaste.containsKey(p)) {
				pasten(aushwalString, rgID, p);
				p.closeInventory();
			} else {
				p.closeInventory();
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noLastPaste"));
			}
		}

	}

	private void facingInv(Player p, Inventory pInv, ItemStack clicked) {
		String south = StringGetterBau.getString(p, "facingSouth");
		String north = StringGetterBau.getString(p, "facingNorth");
		String clickedName = clicked.getItemMeta().getDisplayName();
		if (north.equals(clickedName)) {
			aushwalString = aushwalString + "N_";
		} else if (south.equals(clickedName)) {
			aushwalString = aushwalString + "S_";
		}
		p.openInventory(schildRahmenNormalInventory(p));
	}

	private void typeInv(Player p, Inventory pInv, ItemStack clicked) {
		String normal = "§rNormal";
		String frame = StringGetterBau.getString(p, "frame");
		String shield = StringGetterBau.getString(p, "shield");
		String clickedName = clicked.getItemMeta().getDisplayName();
		if (clickedName.equals(normal)) {
			aushwalString = aushwalString + "N";
		} else if (clickedName.equals(frame)) {
			aushwalString = aushwalString + "F";
		} else if (clickedName.equals(shield)) {
			aushwalString = aushwalString + "S";
		}
		p.closeInventory();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(p.getWorld()));
		String rgID = regions.getApplicableRegionsIDs(
				BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())).get(0);
		pasten(aushwalString, rgID, p);
		playerLastPaste.put(p, aushwalString);
	}

	public Inventory richtungsInventory(Player p) {
		// norden
		
		ItemStack isN = Banner.N.setName(StringGetterBau.getString(p, "facingNorth"));
		// süden
		ItemStack isS = Banner.S.setName(StringGetterBau.getString(p, "facingSouth"));
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveFacingInv"));
		inv.setItem(2, isN);
		inv.setItem(6, isS);
		return inv;
	}

	public Inventory schildRahmenNormalInventory(Player p) {
		// Schild
		ItemStack isSchild = new ItemStack(Material.SHIELD);
		ItemMeta imSchild = isSchild.getItemMeta();
		imSchild.setDisplayName(StringGetterBau.getString(p, "shield"));
		isSchild.setItemMeta(imSchild);
		// Rahmen
		ItemStack isRahmen = new ItemStack(Material.SCAFFOLDING);
		ItemMeta imRahmen = isRahmen.getItemMeta();
		imRahmen.setDisplayName(StringGetterBau.getString(p, "frame"));
		isRahmen.setItemMeta(imRahmen);
		// normal
		ItemStack isNormal = new ItemStack(Material.WHITE_WOOL);
		ItemMeta imNormal = isNormal.getItemMeta();
		imNormal.setDisplayName("§rNormal");
		isNormal.setItemMeta(imNormal);
		// setzen
		Inventory inv = Bukkit.createInventory(null, 9, StringGetterBau.getString(p, "testBlockSklaveTypeInv"));
		inv.setItem(1, isSchild);
		inv.setItem(4, isNormal);
		inv.setItem(7, isRahmen);
		return inv;
	}

	public static void pasten(String auswahl, String regionID, Player p) {
		int ID = Integer.parseInt(regionID.replace("plot", ""));
		try {
			// für jede Zeile rgid festlegen

			int x = -101 - (ID - 1) * 108;
			int y = 8;
			int z = 17;
			// paste
			Clipboard clipboard = createClipboard(auswahl);

			EditSession editSession = (EditSession) WorldEdit.getInstance().getEditSessionFactory()
					.getEditSession(BukkitAdapter.adapt(p.getWorld()), -1);
			editSession.setFastMode(false);
			editSession.enableStandardMode();
			Operation operation;
			if (auswahl.contains("_F")) {
				operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(x, y, z))
						.ignoreAirBlocks(true).build();
			} else {
				operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(x, y, z))
						.ignoreAirBlocks(false).build();
			}

			try {
				Operations.complete(operation);
				WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p)).remember(editSession);
				editSession.flushSession();
			} catch (WorldEditException e) {
				p.sendMessage(
						Main.prefix + "irgendetwas ist schiefgelaufen, überprüfe ob alle dummys eingespeichert sind!");
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Clipboard createClipboard(String filename) {
		Main main = Main.getPlugin();
		String path = main.getCustomConfig().getString("Config.path");
		File pathFile = new File(path);
		path = pathFile.getParentFile().getAbsolutePath();
		File dir = new File(path + "/schematics/TestBlockSklave");
		File schem = new File(dir.getAbsolutePath() + "/" + filename + ".schem");
		ClipboardFormat format = ClipboardFormats.findByFile(schem);
		try {
			ClipboardReader reader = format.getReader(new FileInputStream(schem));
			Clipboard clipboard = reader.read();
			return clipboard;
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return null;

	}

	public static void resetRegion(String rgID, Player p, boolean confirmed) {
		int rgIDint = Integer.parseInt(rgID.replace("plot", ""));
		if (!confirmed) {
			JsonCreater creater1 = new JsonCreater(Main.prefix + 
					StringGetterBau.getString(p, "delePlotConfirmation").replace("%r", ""+rgIDint));
			JsonCreater creater2 = new JsonCreater(StringGetterBau.getString(p, "deletePlotHere").replace("%r", ""+rgIDint));
			creater2.addHoverEvent(StringGetterBau.getString(p, "delePlotHover").replace("%r", ""+rgIDint));
			creater2.addClickEvent("/delcon " + rgID + " " + p.getUniqueId(), ClickAction.RUN_COMMAND);
			creater1.addJson(creater2).send(p);
		} else {
			if(playerBlockedDelete.contains(p)) {
				p.sendMessage(StringGetterBau.getString(p, "deletePlotAntiSpaw"));
				return;
			}
				playerBlockedDelete.add(p);
			p.sendMessage(StringGetterBau.getString(p, "delePlot").replace("%r", ""+rgIDint));
			// für jede Zeile rgid festlegen
			ProtectedRegion rg = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld())).getRegion(rgID);
			int xmin = rg.getMinimumPoint().getBlockX();
			int xmax = rg.getMaximumPoint().getBlockX();
			
			
			int ymax = rg.getMaximumPoint().getBlockY();//ganz oben
			int ymin2 = rg.getMinimumPoint().getBlockY();//ganz unten
			int ymin1 = (ymax-ymin2)/2;//mitte
			int zmin = rg.getMinimumPoint().getBlockZ();
			int zmax = rg.getMaximumPoint().getBlockZ();
			Scheduler scheduler = new Scheduler();
			scheduler.setY(ymax);
			scheduler.setYmin(ymin1);
			scheduler.setZ(zmin);
			World world = p.getWorld();
			// paste
			pasten("ground", rgID, p);
			scheduler.setTask(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

						@Override
						public void run() {
							int z = scheduler.getZ();
							for (int x = xmax; x >= xmin; x--) {
								for (int y = scheduler.getY(); y >= scheduler.getYMin(); y--) {
									Block b = world.getBlockAt(x, y, z);
									if (!b.getType().equals(Material.AIR)) {
										b.setType(Material.AIR);
									}
								}
							}
							if (scheduler.getZ() >= zmax&&scheduler.getY()==(ymin1-1)) {
								scheduler.cancel();
								return;
							}else if(scheduler.getZ() == zmax){
								scheduler.setY(ymin1-1);
								scheduler.setYmin(ymin2);
								scheduler.setZ(zmin);
								return;
							}
							z++;
							scheduler.setZ(z);
						}
					}, 1, 1));
			
			//spamschutz
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(	) {
				
				@Override
				public void run() {
					playerBlockedDelete.remove(p);
				}
			}, 20*60*5);
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
