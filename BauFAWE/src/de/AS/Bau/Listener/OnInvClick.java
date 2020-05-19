package de.AS.Bau.Listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

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
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import de.AS.Bau.DBConnection;
import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.WorldEdit.WorldEditHandler;
import de.AS.Bau.utils.ClickAction;
import de.AS.Bau.utils.JsonCreater;
import de.AS.Bau.utils.Scheduler;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;

public class OnInvClick implements Listener {


	private static HashSet<UUID> playerBlockedDelete = new HashSet<>();

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
			if(playerBlockedDelete .contains(p.getUniqueId())) {
				p.sendMessage(StringGetterBau.getString(p, "deletePlotAntiSpaw"));
				return;
			}
				playerBlockedDelete.add(p.getUniqueId());
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
			WorldEditHandler.pasten("ground", rgID, p,true,false,false);
			//TODO paste without saving in undo
			
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
					playerBlockedDelete.remove(p.getUniqueId());
				}
			}, 20*60*1);
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
